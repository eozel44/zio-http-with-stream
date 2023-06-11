import SpeechServiceSpec.test
import service.SpeechService
import zio.Scope
import zio.http.Client
import zio.stream.ZStream
import zio.test.Assertion.equalTo
import zio.test._

object SpeechServiceSpec extends ZIOSpecDefault {
  def spec =
    suite("SpeechService")(
      test("sum with different values") {
        assertZIO(
          SpeechService.sum(ZStream.fromIterable(List(Some(10L), None, Some(20L), None, Some(10L))))
        )(equalTo(Option.apply(40L)))

      },
        test("sum with none") {
        assertZIO(
          SpeechService.sum(ZStream.fromIterable(List(None, None, None)))
        )(equalTo(Option.empty[Long]))

      },
      test("test link") {
          assertZIO(
            SpeechService.calculateSpeechs(List("https://fid-recruiting.s3-eu-west-1.amazonaws.com/politics.csv"))
          )(equalTo((None,Some("Alexander Abel"),Some("Caesare Collins"))))

        }

    ).provide(Client.default, Scope.default)
}