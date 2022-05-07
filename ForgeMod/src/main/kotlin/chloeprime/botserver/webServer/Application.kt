package chloeprime.botserver.webServer

import chloeprime.botserver.common.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.content.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.apache.http.auth.*

private val verifier = Auth.makeJwtVerifier()

fun Application.module() {
//    install(DataConversion)
    install(ContentNegotiation) {
        gson {
        }
    }

    // 添加状态页
    install(StatusPages) {
        exception<InvalidCredentialsException> { exception ->
            call.respond(
                HttpStatusCode.Unauthorized,
                mapOf("OK" to false, "error" to (exception.message ?: ""))
            )
        }
        status(HttpStatusCode.NotFound) {
            call.respond(
                TextContent(
                    "${it.value} ${it.description}",
                    ContentType.Text.Plain.withCharset(Charsets.UTF_8),
                    it
                )
            )
        }

        status(HttpStatusCode.Unauthorized) {
            call.respond(
                TextContent(
                    "${it.value} ${it.description}",
                    ContentType.Text.Plain.withCharset(Charsets.UTF_8),
                    it
                )
            )
        }
    }

    //安装JWT模块
    install(Authentication) {
        jwt {
            verifier(verifier)
            validate {
                UserIdPrincipal(it.payload.getClaim("name").asString())
            }
        }
    }
    routing {
        login()
        api()
    }
}


fun Route.login() {
    post("/login") {
        val user = call.receive<User>()
        val password = ModConfig.INSTANCE.webApiUserNameAndPassword[user.name]
            ?: throw InvalidCredentialsException("Invalid credentials")
        if (user.password != password)
            throw InvalidCredentialsException("Invalid credentials")

        call.respond(Auth.sign(user.name))
    }
}

fun Route.api() {
    authenticate {
        route("/api") {
            get {
                val principal = call.principal<UserIdPrincipal>() ?: error("No principal")
                call.respond("用户: ${principal.name} 欢迎来到MCGWebAPI！")
            }
            post {
                val principal = call.principal<UserIdPrincipal>() ?: error("No principal")
                val receive = call.receive<RequestPO>()

                RequestDispatcher.dispatchRequest(receive, call)
            }
        }
    }
}