/*
 * Copyright 2020-2021 Typelevel
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

package cats.effect.std

import cats.syntax.all._
import cats.effect.kernel.Sync
import cats.effect.std.syntax.Env

import scala.scalajs.js
import scala.collection.immutable.Map
import scala.util.Try
import cats.data.OptionT

private[std] class EnvCompanionPlatform {
  private[std] final class SyncEnv[F[_]](implicit F: Sync[F]) extends Env[F] {
    def get(name: String): F[Option[String]] =
      OptionT(F.delay(processEnv.get(name))).collect {
        case value: String => value // JavaScript. 'nuff said
      }.value

    def toMap: F[Map[String, String]] =
      F.delay(processEnv.collect { case (name, value: String) => name -> value }.toMap)

    private def processEnv =
      Try(js.Dynamic.global.process.env.asInstanceOf[js.Dictionary[Any]])
        .getOrElse(js.Dictionary.empty)
  }
}
