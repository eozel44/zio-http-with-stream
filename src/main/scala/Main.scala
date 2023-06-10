import zio._
import zio.http.Client

object Main extends ZIOAppDefault {

  import service._

  val url = "https://fid-recruiting.s3-eu-west-1.amazonaws.com/politics.csv"

  val program = for {

    res <- SpeechService.calculateSpeechs(url)

  } yield res

  override val run = program.provide(Client.default)

}
