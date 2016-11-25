package io.soheila.commons.entities

/**
 * Helper for pagination.
 * Paging is 0 based. This class optionally provides next and previous pages.
 */
case class Page[A](items: Seq[A], page: Int, offset: Int, total: Int) {
  lazy val prev: Option[Int] = Option(page - 1).filter(_ >= 0)
  lazy val next: Option[Int] = Option(page + 1).filter(_ => (offset + items.size) < total)
}
