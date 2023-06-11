import domain.ServiceError
import zio._
import zio.http.HttpError.InternalServerError
import zio.http._

object Main extends ZIOAppDefault {

  import service._

  val urls = List(
    "https://fid-recruiting.s3-eu-west-1.amazonaws.com/politics.csv",
    "https://fid-recruiting.s3-eu-west-1.amazonaws.com/politics.csv",
    "https://fid-recruiting.s3-eu-west-1.amazonaws.com/politics.csv"
  )

  val app: HttpApp[Client & Scope, Nothing] = Http.collectZIO[Request] { case Method.GET -> Root / "evaluation" =>
    SpeechService
      .calculateSpeechs(urls)
      .foldZIO(
        { case error: ServiceError =>
          Console.printLine(error).orDie *>
            ZIO.succeed(Response.fromHttpError(InternalServerError("Internal error : " + error.getMessage)))
        },
        result =>
          ZIO.succeed(
            Response
              .text(s"""{"mostSecurity": "${result._1}","mostSpeeches": "${result._2}","leastWordy": "${result._3}"}""")
          )
      )
  }

  override val run =
    Server.serve(app).provide(Client.default, Scope.default, Server.defaultWithPort(8090))

}
