package io.beatmaps.bookmarksync

import external.axiosGet
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import org.w3c.dom.HTMLInputElement
import react.Props
import react.dom.a
import react.dom.div
import react.dom.input
import react.fc
import react.useEffect
import react.useState

data class IndexEntry(val name: String, val songs: Int) {
    val downloadUrl by lazy {
        "/playlist/${name[0]}/${name}.bplist"
    }

    fun matches(search: String) = name.contains(search, true)
}

val homePage = fc<Props> {
    val (search, setSearch) = useState("")
    val (results, setResults) = useState(listOf<IndexEntry>())
    val (index, setIndex) = useState(listOf<IndexEntry>())
    val (idx, setIdx) = useState(0)
    val maxResults = 20

    useEffect(search, index) {
        // Return exact result at top if possible (may fail due to split index)
        val exactResult = index.firstOrNull { it.name.equals(search, true) }
        setResults(
            (listOfNotNull(exactResult) + index.filter { it.matches(search) }).take(maxResults)
        )
    }

    useEffect(results) {
        if (results.size < maxResults && idx < 5) {
            setIdx(idx + 1)
            axiosGet<Map<String, Int>>("/static/index/index$idx.json").then {
                setIndex(index + it.map { (n, s) -> IndexEntry(n, s) })
            }
        }
    }

    input(InputType.text) {
        attrs.onChangeFunction = {
            val text = (it.currentTarget as? HTMLInputElement)?.value ?: ""
            setSearch(text)
        }
    }

    div("results") {
        results.forEach {
            a(it.downloadUrl, "_blank") {
                +it.name
                div {
                    +"${it.songs}"
                }
            }
        }
    }
}