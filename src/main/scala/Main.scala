import domain.ServiceError
import zio._
import zio.http.HttpError.InternalServerError
import zio.http._

object Main extends ZIOAppDefault {

  import service._

  val url = "https://fid-recruiting.s3-eu-west-1.amazonaws.com/politics.csv"

  val app: HttpApp[Client, Nothing] = Http.collectZIO[Request] { case Method.GET -> Root / "text" =>
    SpeechService
      .calculateSpeechs(url)
      .fold(
        { case error: ServiceError =>
          Response.fromHttpError(InternalServerError("Internal error : " + error.getMessage))
        },
        message => Response.text(message)
      )
  }

  override val run =
    Server.serve(app).provide(Client.default, Server.defaultWithPort(8090))

}
