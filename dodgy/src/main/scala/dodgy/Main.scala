package dodgy

import typings.phaser.Phaser.Types.Core.{ GameConfig, PhysicsConfig, ScaleConfig }
import typings.phaser.Phaser.Types.Physics.Arcade.ArcadeWorldConfig
import typings.phaser.phaserMod.Game
import typings.phaser.{ phaserMod => Phaser }

import scala.scalajs.js.annotation._

@JSExportTopLevel("dodgy")
object Main {
  val debugPhysics = false

  val config = GameConfig()
    .setType(Phaser.AUTO)
    .setParent("game")
    .setWidth(1000)
    .setHeight(1000)
    .setPhysics(PhysicsConfig().setDefault("arcade").setArcade(ArcadeWorldConfig().setDebug(debugPhysics)))
    .setScale(ScaleConfig().setMode(Phaser.Scale.ScaleModes.FIT))
    .setScene(new MainScene)

  def main(args: Array[String]): Unit =
    new Game(config)

}
