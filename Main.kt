import com.mpatric.mp3agic.Mp3File
import java.io.File

fun main(){
    val path = "enter_your_path"
    val pattern = Regex("""^.*\.mp3$""")
    val files = findAllMp3(path, pattern)
    files.printAll()
    files.printDuplicatesByName()
}

fun findAllMp3(dirPath: String, pattern: Regex): Sequence<Mp3Extended> {
    val d = File(dirPath)
    require (d.exists() && d.isDirectory)
    return d.walkTopDown()
        .map { it.path }.filter { it.matches(pattern) }
        .map { Mp3Extended(it, Mp3File(it)) }
        .sortedWith(compareBy<Mp3Extended> { it.artist }.thenBy { it.album })
}

fun Sequence<Mp3Extended>.printAll(){
    println("All files")
    this.forEach { println(it) }
}

fun Sequence<Mp3Extended>.printDuplicatesByName(){
    println("Duplicates by name")
    this.groupBy { it.stamp } //equals?
        .onEach { it.toPair() }
        .filter { it.value.size > 1 }
        .onEach { it.printGroup() }
}


fun Map.Entry<String, List<Mp3Extended>>.printGroup(){
    println("${this.key}\n ${this.value}")
}

data class Mp3Extended(val path: String, val file: Mp3File) {


    var album: String? = "Not supported"
    var artist: String? = "Not supported"
    var title: String? = "Not supported"
    var duration: Long
    var stamp: String
    init {
        if (file.hasId3v2Tag()) {
            title = file.id3v2Tag.title?:"Unknown title"
            artist = file.id3v2Tag.albumArtist?:"Unknown artist"
            album = file.id3v2Tag.album?:"Unknown album"
        } else if(file.hasId3v1Tag()){
            title = file.id3v1Tag.title?:"Unknown title"
            artist = file.id3v1Tag.artist?:"Unknown artist"
            album = file.id3v1Tag.album?:"Unknown album"
        }
        duration = file.lengthInMilliseconds
        stamp = "$artist - $album - $title" //equals??
    }

    fun Long.toMinutes(): String {
        val minutes = this / 1000 / 60
        val seconds = this / 1000 % 60
        if (seconds < 10) return "$minutes:0$seconds"
        else return "$minutes:$seconds"
    }

    override fun toString(): String {
        return "$artist - $album - $title - ${duration.toMinutes()} - $path"
    }
}