# ZHttp & ZStream 

Build http server with ZIO, ZStream and ZHttp

### dependencies:

```scala
val scala2                  = "2.13.8"
val zio                     = "2.0.15"
val zioHttp                 = "3.0.0-RC2"
val zioNio                  = "2.0.0"
val zioConfig               = "3.0.2"
val zioLogging              = "2.1.0"
val zioPrelude              = "1.0.0-RC14"
val scalatest               = "3.2.15"
val logbackClassic          = "1.2.11"
val circe                   = "0.14.2"
```

### code:

```scala
 def calculateSpeechs(urls: List[String]) =
  for {

    zstreams <- ZStream
      .fromIterable(urls)
      .flatMapPar(2)(url =>
        ZStream
          .fromZIO(Client.request(url).mapError(k => HttpClientError(k.getMessage)))
          .flatMap(l => l.body.asStream)
          .via(ZPipeline.utf8Decode >>> ZPipeline.splitLines)
          .via(ZPipeline.drop(1))
      )
      .buffer(100)
      .mapZIOParUnordered(4)(in =>
        ZIO.fromEither(decode[Speech](convertToJson(in))).mapError(k => ParsingError(k.getMessage()))
      )
      .broadcast(2, 30)

    mostSpeechs   <- calculate(
      zstreams(0).filter(in => in.dateOfSpeech.get(ChronoField.YEAR).equals(2013))
    ).runCollect.fork   
    leastWordy    <- calculate(zstreams(2)).runCollect.fork

    zipped <- mostSpeechs.zip(leastWordy).join

  } yield (
    zipped._1.sortWith(_._2.getOrElse(0L) > _._2.getOrElse(0L)).headOption.map(_._1),    
    zipped._2.sortWith(_._2.getOrElse(0L) < _._2.getOrElse(0L)).headOption.map(_._1)
  )
```
### run:

sbt run


### keywords:
zio, zhttp, zstream
