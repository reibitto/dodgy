package dodgy

sealed trait GameState

object GameState {
  case object Playing extends GameState
  case object Dead    extends GameState
}
