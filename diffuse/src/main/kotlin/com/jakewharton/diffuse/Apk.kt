package com.jakewharton.diffuse

import com.jakewharton.diffuse.ArchiveFile.Type.Companion.toApkFileType
import com.jakewharton.diffuse.ArchiveFiles.Companion.toArchiveFiles
import com.jakewharton.diffuse.Arsc.Companion.toArsc
import com.jakewharton.diffuse.Dex.Companion.toDex
import com.jakewharton.diffuse.Manifest.Companion.toManifest
import com.jakewharton.diffuse.Signatures.Companion.toSignatures
import com.jakewharton.diffuse.io.Input

class Apk private constructor(
  override val filename: String?,
  val files: ArchiveFiles,
  val dexes: List<Dex>,
  val arsc: Arsc,
  val manifest: Manifest,
  val signatures: Signatures
) : Binary {
  companion object {
    internal val classesDexRegex = Regex("classes\\d*\\.dex")
    internal const val resourcesArscFileName = "resources.arsc"
    internal const val manifestFileName = "AndroidManifest.xml"

    @JvmStatic
    @JvmName("parse")
    fun Input.toApk(): Apk {
      val signatures = toSignatures()
      toZip().use { zip ->
        val files = zip.toArchiveFiles { it.toApkFileType() }
        val arsc = zip[resourcesArscFileName].asInput().toArsc()
        val manifest = zip[manifestFileName].asInput().toBinaryResourceFile().toManifest(arsc)
        val dexes = zip.entries
            .filter { it.path.matches(classesDexRegex) }
            .map { it.asInput().toDex() }
        return Apk(name, files, dexes, arsc, manifest, signatures)
      }
    }
  }
}
