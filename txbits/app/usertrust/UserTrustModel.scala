// TxBits - An open source Bitcoin and crypto currency exchange
// Copyright (C) 2014-2015  Viktor Stanchev & Kirk Zathey
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package usertrust

import play.api.db.DB
import securesocial.core.Token
import java.sql.Timestamp
import play.api.Play.current
import models.Withdrawal
import org.joda.time.DateTime

class UserTrustModel(val db: String = "default") {
  def getTrustedActionRequests = DB.withConnection(db) { implicit c =>
    SQLText.getTrustedActionRequests().map(row =>
      (row[String]("email"), row[Boolean]("is_signup"))
    ).toList
  }

  def getPendingWithdrawalRequests = DB.withConnection(db) { implicit c =>
    SQLText.getPendingWithdrawalRequests().map(row =>
      (
        Withdrawal(
          row[Long]("id"),
          row[BigDecimal]("amount").bigDecimal.toPlainString,
          row[BigDecimal]("fee").bigDecimal.toPlainString,
          row[DateTime]("created"),
          "",
          row[String]("currency")
        ),
          row[String]("email"),
          row[Option[String]]("pgp"),
          row[Option[String]]("destination")
      )
    ).toList
  }

  def saveWithdrawalToken(id: Long, token: String, expiration: DateTime) = DB.withConnection(db) { implicit c =>
    SQLText.saveWithdrawalToken.on('id -> id, 'token -> token, 'expiration -> new Timestamp(expiration.getMillis)).execute
  }

  def trustedActionFinish(email: String, is_signup: Boolean) = DB.withConnection(db) { implicit c =>
    SQLText.trustedActionProcessed.on('email -> email, 'is_signup -> is_signup).execute
  }

  def saveToken(token: Token) = DB.withConnection(db) { implicit c =>
    SQLText.saveToken.on(
      'email -> token.email,
      'token -> token.uuid,
      'creation -> new Timestamp(token.creationTime.getMillis),
      'expiration -> new Timestamp(token.expirationTime.getMillis),
      'is_signup -> token.isSignUp
    ).execute
  }
}
