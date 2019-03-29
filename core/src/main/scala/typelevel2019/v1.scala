package typelevel2019

import cats._, cats.data._, cats.implicits._
import io.circe._, io.circe.syntax._, io.circe.generic.auto._
import scala.concurrent._

package object hockey {
  implicit val ec: ExecutionContext = new ExecutionContext {
    def execute(runnable: Runnable) = runnable.run()
    def reportFailure(cause: Throwable) = cause.printStackTrace()
  }

  def renderPlayer(p: Player): Json = p.asJson

  def crossCheck(p: Player) = p
  def slash(p: Player) = p
}

package hockey {
  case class Player(firstName: String, lastName: String, position: String, team: String)
  case class Stats(games: Int, goals: Int, assists: Int)

  trait HockeyDb[F[_]] {
    def captain(team: String): F[Player]
    def stats(lastName: String): F[Stats]
  }

  object HockeyDb {
    def apply[F[_]](implicit ev: HockeyDb[F]) = ev

    private val captains = Map(
      "PHI" -> Player("Claude", "Giroux", "RW", "PHI"),
      "BOS" -> Player("Zdeno", "Chara", "D", "BOS")
    )
    private val statsMap = Map(
      "Giroux" -> Stats(812,  234, 522),
      "Chara"  -> Stats(1478, 199, 440)
    )

    implicit val option: HockeyDb[Option] = new HockeyDb[Option] {
      def captain(team: String): Option[Player] = captains.get(team)
      def stats(lastName: String): Option[Stats] = statsMap.get(lastName)
    }

    implicit val future: HockeyDb[Future] = new HockeyDb[Future] {
      def captain(team: String): Future[Player] = captains.get(team) match {
        case Some(player) => Future.successful(player)
        case None => Future.failed(new NoSuchElementException())
      }

      def stats(lastName: String): Future[Stats] = statsMap.get(lastName) match {
        case Some(stats) => Future.successful(stats)
        case None => Future.failed(new NoSuchElementException())
      }
    }

    type FO[A] = Future[Option[A]]
    implicit val futureOption: HockeyDb[FO] = new HockeyDb[FO] {
      def captain(team: String): FO[Player] = Future.successful(option.captain(team))
      def stats(lastName: String): FO[Stats] = Future.successful(option.stats(lastName))
    }

    type NFO[A] = Nested[Future, Option, A]
    implicit val nestedFutureOption: HockeyDb[NFO] = new HockeyDb[NFO] {
      def captain(team: String): NFO[Player] = Nested(futureOption.captain(team))
      def stats(lastName: String): NFO[Stats] = Nested(futureOption.stats(lastName))
    }
  }
}
