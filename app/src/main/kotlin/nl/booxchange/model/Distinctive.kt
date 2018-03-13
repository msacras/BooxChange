package nl.booxchange.model

/**
 * Created by Cristian Velinciuc on 3/11/18.
 */
abstract class Distinctive {
  abstract val id: Int
  override fun equals(other: Any?): Boolean {
    return if (other is Distinctive) {
      this.id == other.id
    } else false
  }

  override fun hashCode(): Int {
    return this.id
  }
}
