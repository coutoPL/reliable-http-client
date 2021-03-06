/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rhttpc.transport.amqp

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import com.rabbitmq.client.Connection
import rhttpc.transport.PubSubTransport
import rhttpc.transport.json4s.Json4sHttpRequestResponseFormats
import rhttpc.transport.protocol.Correlated

import scala.util.Try

object AmqpHttpTransportFactory {
  def createRequestResponseTransport(connection: Connection)
                                    (implicit actorSystem: ActorSystem): PubSubTransport[Correlated[HttpRequest]] = {
    import Json4sHttpRequestResponseFormats._
    AmqpTransportFactory.create(
      AmqpTransportCreateData(connection)
    )
  }

  def createResponseRequestTransport(connection: Connection)
                                    (implicit actorSystem: ActorSystem): PubSubTransport[Correlated[Try[HttpResponse]]] = {
    import Json4sHttpRequestResponseFormats._
    AmqpTransportFactory.create(
      AmqpTransportCreateData(connection)
    )
  }
}