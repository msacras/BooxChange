package nl.booxchange.model

import java.io.Serializable


interface FirestoreObject: Serializable {
  val id: String
}
