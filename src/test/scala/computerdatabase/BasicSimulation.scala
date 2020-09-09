package computerdatabase

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class BasicSimulation extends Simulation {

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


  val oneRegenerateRequest = exec(http("regenerate-token")
    .get("/casc-bundle/regenerate-token?bundleId=${name}")
    .basicAuth("admin","11f0d873624a87b01f98a7259215aca6d8")
    .check(status.is(200)))
    .pause(1,2)


  val refreshTokenScenario = scenario("/casc-bundle/regenerate-token")
    .feed(bundleNameFeed)
    .repeat(3) {
      oneRegenerateRequest
    }

  setUp(
    callListScenario.inject(rampUsers(3) during(1 minute)),
    refreshTokenScenario.inject(atOnceUsers(2))
  ).protocols(httpProtocol)

}
