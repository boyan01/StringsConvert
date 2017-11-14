package tech.summerly

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.dom4j.io.DOMReader
import java.io.File
import java.io.FileOutputStream
import javax.xml.parsers.DocumentBuilderFactory

private const val DIR_OUT = "./"
private const val DIR_INPUT = "./strings/"

fun main(args: Array<String>) {

    val dir = File(DIR_INPUT).takeIf { it.exists() && it.isDirectory }
            ?: error("can not find strings directory")
    val files = dir.listFiles().filter { it.name.endsWith(".xml") }
    val names = files.map { it.nameWithoutExtension }

    val dbf = DocumentBuilderFactory.newInstance()
    val db = dbf.newDocumentBuilder()
    val list = files.map { db.parse(it) }
            .map { DOMReader().read(it) }
            .map { it.rootElement }
            .map { it.elements() }

    val map = mutableMapOf<String, StringTag>()


    list.forEachIndexed { index, element ->
        val name = names[index]
        element.forEach {
            val id = it.attributeValue("name")
            val tag = map[id]
            if (tag == null) {
                map[id] = StringTag(id).apply {
                    values.put(name, it.stringValue)
                }
            } else {
                tag.values.put(name, it.stringValue)
            }
        }
    }
    val hssfWorkbook = HSSFWorkbook()
    val sheet = hssfWorkbook.createSheet("strings")
    val rowHeader = sheet.createRow(0)
    rowHeader.createCell(0).setCellValue("id")
    names.forEachIndexed { index, title ->
        rowHeader.createCell(index + 1).setCellValue(title)
    }
    var rowNum = 1
    map.forEach { key, stringTag ->
        val row = sheet.createRow(rowNum)
        row.createCell(0).setCellValue(key)
        names.forEachIndexed { index, s ->
            stringTag.values[s]?.let {
                row.createCell(index + 1).setCellValue(it)
            }
        }
        rowNum++
    }
    val output = File(DIR_OUT, "strings.xls")
    if (output.exists()) {
        println("detected strings.xls file exists, perform to delete it")
        output.delete()
    }
    val outputStream = FileOutputStream(output)
    hssfWorkbook.write(outputStream)
    outputStream.close()

    println("covert success !")
}

class StringTag(val name: String) {
    val values: MutableMap<String, String> = mutableMapOf()
    override fun toString(): String {
        return "StringTag(name='$name', values=$values)"
    }


}