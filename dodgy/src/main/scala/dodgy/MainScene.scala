package dodgy

import typings.phaser.Phaser.GameObjects.Text
import typings.phaser.Phaser.Loader.LoaderPlugin
import typings.phaser.Phaser.Physics.Arcade
import typings.phaser.Phaser.Types.GameObjects.Text
import typings.phaser.Phaser.Types.Input.Keyboard.CursorKeys
import typings.phaser.Phaser.Types.Physics.Arcade.SpriteWithDynamicBody
import typings.phaser.phaserMod.Scene

import scala.collection.mutable
import scala.scalajs.js
import scala.util.Random

class MainScene extends Scene {
  var player: SpriteWithDynamicBody = _
  var fpsCounter: Text              = _
  var scoreLabel: Text              = _
  var youDiedLabel: Text            = _
  var restartLabel: Text            = _
  var scalaGroup: Arcade.Group      = _
  var cursorKeys: CursorKeys        = _

  var score: Double         = 0
  var gameState: GameState  = GameState.Playing
  var restartMouseDown: Int = 0

  val startPlayerY: Int = 940

  val scalaRain: mutable.Set[SpriteWithDynamicBody] = mutable.Set.empty

  val preload: js.ThisFunction0[Scene, LoaderPlugin] = { scene =>
    scene.load.image("scala", "./scala.png")
    scene.load.image("person", "./person.png")
  }

  val create: js.ThisFunction1[Scene, js.Object, Unit] = (scene, data) => {
    player = scene.physics.add
      .sprite(500, startPlayerY, "person")
      .setScale(0.4)
      .setBodySize(70, 250)

    fpsCounter =
      scene.add.text(860, 25, "FPS: n/a", Text.TextStyle().setFontSize("28px").setColor("#5BFF6C")).setDepth(1)
    scoreLabel = scene.add.text(25, 25, "Score: 0", Text.TextStyle().setFontSize("28px")).setDepth(1)

    youDiedLabel = scene.add
      .text(
        250,
        450,
        "YOU DIED",
        Text.TextStyle().setFontSize("100px").setColor("#ff0000").setStroke("#000").setStrokeThickness(5)
      )
      .setDepth(1)
      .setVisible(false)

    restartLabel = scene.add
      .text(
        230,
        550,
        "Press space or click to restart",
        Text.TextStyle().setFontSize("28px").setStroke("#000").setStrokeThickness(3)
      )
      .setDepth(1)
      .setVisible(false)

    scalaGroup = physics.add.group()
    cursorKeys = input.keyboard.createCursorKeys()
  }

  override def update(time: Double, delta: Double): Unit = {
    gameState match {
      case GameState.Dead =>
        youDiedLabel.setVisible(true)
        restartLabel.setVisible(true)
        physics.pause()

        if (cursorKeys.space.isDown)
          restartGame()

        // Awful bitmask approach, forgive me. I'm in a hurry.
        if (input.activePointer.isDown) {
          if (restartMouseDown != 0) {
            restartMouseDown |= 1
          }
        } else {
          restartMouseDown |= 2
        }

        // If transitioned from mouse up to mouse down then we can restart
        if (restartMouseDown >= 3)
          restartGame()

      case GameState.Playing =>
        if (game.isRunning)
          score += delta

        if (Random.nextDouble() <= 0.0035 * delta + score / 700000) {
          addDroplet()
        }

        scalaRain.filter(_.y > 1000).foreach { droplet =>
          removeDroplet(droplet)
        }

        player.setX(input.activePointer.x max 25 min (1000 - 25))
    }

    fpsCounter.text = s"FPS: ${game.loop.actualFps.floor}"
    scoreLabel.text = s"Score: ${(score / 100).floor}"

    if (physics.collide(player, scalaGroup)) {
      gameState = GameState.Dead
    }
  }

  def addDroplet(): Unit = {
    val sprite = physics.add
      .sprite(Random.between(20, 980.0), Random.between(-200, -150), "scala")
      .setBodySize(62, 84)

    scalaRain.add(sprite)
    scalaGroup.add(sprite)

    sprite
      .setVelocityY(750 + score / 200)
      .setMass(0.0001)
  }

  def removeDroplet(sprite: SpriteWithDynamicBody): Unit = {
    scalaRain.remove(sprite)
    sprite.destroy(true)
  }

  def restartGame(): Unit = {
    gameState = GameState.Playing
    score = 0
    restartMouseDown = 0

    youDiedLabel.visible = false
    restartLabel.visible = false

    scalaRain.foreach { r =>
      r.destroy(true)
    }

    scalaGroup.clear(true)
    scalaRain.clear()

    player.setPosition(input.activePointer.x, startPlayerY)
    player.setVelocity(0, 0)

    physics.resume()
  }
}
