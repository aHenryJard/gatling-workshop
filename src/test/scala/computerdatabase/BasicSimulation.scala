package computerdatabase

import io.gatling.core.Predef._
import io.gatling.http.Predef.{http, _}

import scala.concurrent.duration._
import io.gatling.core.Predef.Simulation

/**
  * Create from Gatling Recording, with some constants to easy switch to another Jenkins instance.
  *
  * Scenario is:
  * - login
  * - then loop during TEST_DURATION on :
  *   - go on /
  *   - go on a job page
  */
class BasicSimulation extends Simulation {

  /* All hard-coded values are here */
  val TEST_DURATION = 2 minutes
  val BASE_URL = "http://localhost:8080"
  val CONTEXT = "/jenkins"
  val USER_COUNT = 2
  val RAMP_DURATION = 1 minute

  val httpProtocol = http
    .baseUrl(BASE_URL)
    //Comment this if you don't want static resources as css, js, png....
    .inferHtmlResources()
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .userAgentHeader("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:88.0) Gecko/20100101 Firefox/88.0")

  val headers_http = Map(
    "Origin" -> BASE_URL,
    "Upgrade-Insecure-Requests" -> "1")

  val headers_ajax = Map(
    "Accept" -> "text/javascript, text/html, application/xml, text/xml, */*",
    "X-Prototype-Version" -> "1.7",
    "X-Requested-With" -> "XMLHttpRequest")

  //only admin so far, but who know for the future...
  val userFeeder = csv("users.csv").random.circular

  //URLs of job to visit the page
  val jobPathFeeder = csv("jobs.csv").random.circular

  val loginAndWalkScenario = scenario("login-and-visit-jobs")
    .feed(userFeeder)
    .exec(http("GET /")
      .get(CONTEXT+"/")
      .headers(headers_http)
      .resources(http(CONTEXT+"/login")
        .get(CONTEXT+"/login?from=%2Fjenkins%2F")
        .headers(headers_http))
      .check(status.is(403)))
    .pause(1,3)
    .exec(http(CONTEXT+"/j_spring_security_check")
      .post(CONTEXT+"/j_spring_security_check")
      .headers(headers_http)
      .formParam("j_username", "${username}") //from userFeeder csv
      .formParam("j_password", "${password}") //from userFeeder csv
      .formParam("from", CONTEXT+"/")
      .formParam("Submit", "Sign in"))

    .during(TEST_DURATION) {
      feed(jobPathFeeder)
      .exec(http("GET /")
          .get(CONTEXT+"/")
          .headers(headers_http))
      .exec(
        http("GET /job/${jobPath}")
          .get(CONTEXT+"/job/${jobPath}")
          .headers(headers_http))
        .pause(1,3)
    }

  setUp(loginAndWalkScenario.inject(rampUsers(USER_COUNT) during(RAMP_DURATION))).protocols(httpProtocol)

}
