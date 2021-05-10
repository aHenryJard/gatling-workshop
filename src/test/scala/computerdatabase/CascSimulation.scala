package computerdatabase

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class CascSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8200") // Here is the root for all relative URLs
  //.basicAuth("admin","11f0d873624a87b01f98a7259215aca6d8")

  val callListScenario = scenario("/casc-bundle/list")
    .exec(http("list_error_in_purpose")
      .get("/casc-bundle/list").check(status.is(200)))
    .pause(1,3)
    .exec(http("list_refused")
      .get("/casc-bundle/list").check(status.is(401)))
    .pause(1,3)
    .exec(http("list_with_admin")
      .get("/casc-bundle/list")
      .basicAuth("admin","11f0d873624a87b01f98a7259215aca6d8").check(status.is(200)))

  val bundleNameFeed = csv("bundle-name.csv").random.circular


  val oneRegenerateRequest = exec(http("regenerate-token_${name}")
    .post("/casc-bundle/regenerate-token?bundleId=${name}")
    .basicAuth("admin","11f0d873624a87b01f98a7259215aca6d8")
    .check(status.is(200)))
    .pause(1,2)


  val refreshTokenScenario = scenario("/casc-bundle/regenerate-token")
    .feed(bundleNameFeed)
    .repeat(3) {
      oneRegenerateRequest
    }

  //Disabling this one for now with some 0
  setUp(
    callListScenario.inject(rampUsers(0) during(1 second)),
    refreshTokenScenario.inject(atOnceUsers(0))
  ).protocols(httpProtocol)

}
