package lv.chi.giffoid.api

data class Envelope<D>(val data: D)
data class EnvelopeList<D>(val data: List<D>)