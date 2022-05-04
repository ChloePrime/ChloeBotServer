package chloeprime.botserver.webServer

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.content.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*

private val verifier = Auth.makeJwtVerifier()

fun Application.module() {
    install(DataConversion)
    install(ContentNegotiation) {
        gson {
        }
    }

    // 添加状态页
    install(StatusPages) {
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

    install(Authentication) {
        jwt {
            verifier(verifier)
            validate {
                UserIdPrincipal(it.payload.getClaim("name").asString())
            }
        }
    }
    routing{
        login()
    }
}


fun Route.login(){
    get("/login") {
        call.respond("登录成功")
    }
}