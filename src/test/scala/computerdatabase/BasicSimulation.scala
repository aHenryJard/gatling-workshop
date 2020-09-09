package computerdatabase

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class BasicSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8200") // Here is the root for all relative URLs

  val scn = scenario("Call /casc-bundle/list twice") // A scenario is a chain of requests and pauses
    .exec(http("list_1")
    .get("/casc-bundle/list"))
    .pause(7)
    .exec(http("list_2")
    .get("/casc-bundle/list"))

  setUp(scn.inject(atOnceUsers(1)).protocols(httpProtocol))





}
