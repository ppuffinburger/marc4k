# MARC4K ![Build Status](https://github.com/ppuffinburger/marc4k/workflows/Build/badge.svg)

## Mission

The goal of MARC4K is to provide an easy to use Application Programming Interface (API) for working with MARC and MARCXML in Kotlin. This project is a Kotlin port of [MARC4J](https://github.com/marc4j/marc4j "https://github.com/marc4j/marc4j"). MARC stands for MAchine Readable Cataloging and is a widely used exchange format for bibliographic data. MARCXML provides a loss-less conversion between MARC (MARC21 but also other formats like UNIMARC) and XML.

## Background

MARC4K came about as a way to learn Kotlin while using a familiar industry standard due to working in the Library ILS/Data Services industry.

## Features

The MARC4K library includes:

* An easy to use interface that can handle large record sets.
* Readers and writers for both MARC and MARCXML.
* A build-in pipeline model to pre- or post-process MARCXML using XSLT stylesheets.
* A MARC record object model (like DOM for XML) for in-memory editing of MARC records.
* Support for data conversions from MARC8 to UCS/Unicode and back.
* Implementation independent XML support through JAXP and SAX2, a high performance XML interface.
* Support for conversions between MARC and MARCXML.
* Tight integration with the JAXP, DOM and SAX2 interfaces.
* Easy to integrate with other XML interfaces like DOM, XOM, JDOM or DOM4J.
* Conversion of the MarcMaker/MarcBreaker format in both MARC8 and UCS/Unicode

MARC4K provides readers and writers for MARC and MARCXML. A `org.marc4k.io.MarcReader` implementation parses input data and provides an iterator over a collection of `org.marc4k.marc.Record` objects. The record object model is also suitable for in-memory editing of MARC records, just as DOM is used for XML editing purposes. Using a `org.marc4k.io.MarcWriter` implementation it is possible to create MARC or MARCXML. Once MARC data has been converted to XML you can further process the result with XSLT, for example to convert MARC to [MODS](https://www.loc.gov/standards/mods/ "MODS").

## Differences from MARC4J

Differences in MARC4K over MARC4J include:

* MARC4K implements MARC21 extensions over the base MarcRecord (Bibliographic, Authority, Classification, Community, and Holdings records). The Marc21StreamReader can wrap another reader and emit these record classes.
* MARC4K has an alternative Reader/Writer implementation that moves the encoding logic into codecs. There is a DefaultMarcDataDecoder/Encoder that operates much like MARC4J's default.
* The Permissive Reader to handle major structural and encoding issues (some issues are handled with the regular reader) is not yet implemented.
* Data conversions to/from Unicode from encodings other than MARC8 or standard encodings is not yet implemented.
* Command-line utilities are not yet created.

## Basic Usage


Reading MARC8 records with MarcStreamReader
```
    MarcStreamReader(FileInputStream(filename), converter = Marc8ToUnicode(true)).use {
        for (record in it) {
            println(record)
        }
    }
```

Reading UTF8 records with MarcStreamReader
```
    MarcStreamReader(FileInputStream(filename), encoding = "UTF-8").use {
        for (record in it) {
            println(record)
        }
    }
```

Reading MARC21 records (uses LDR/09 for decoding) with NewMarcStreamReader
```
    NewMarcStreamReader(FileInputStream(filename), Marc21DataDecoder()).use {
        for (record in it) {
            println(record)
        }
    }
```

Writing MARC8 records with MarcStreamWriter
```
    MarcStreamWriter(output, converter = UnicodeToMarc8()).use {
        it.write(record)
    }
```

Writing UTF8 records with MarcStreamWriter
```
    MarcStreamWriter(output, encoding = "UTF-8").use {
        it.write(record)
    }
```

Writing MARC21 records (uses LDR/09 for encoding) with NewMarcStreamWriter
```
    NewMarcStreamWriter(output, encoder = Marc21DataEncoder()).use {
        it.write(record)
    }
```

## About MARC

MARC stands for MAchine Readable Cataloguing. The MARC format is a popular exchange format for bibliographic records. The structure of a MARC record is defined in the ISO 2709:1996 (Format for Information Exchange) standard (or ANSI/NISO Z39.2-1994, available [online](http://groups.niso.org/apps/group_public/download.php/16342/Z39-2-1994_r2016.pdf "ANSI/NISO Z39.2-1994 (R2016) Information Interchange Format") from NISO). The MARC4K API is not a full implementation of the ISO 2709:1996 standard. The standard is implemented as it is used in the MARC formats.

The most popular MARC formats are MARC21 and UNIMARC. The MARC21 format is maintained by the [Library of Congress](https://www.loc.gov "Library of Congress"). If you're not familiar with MARC21, you might want to visit the [MARC Standards](https://www.loc.gov/marc/ "MARC Standards") page on the Library of Congress website for information on the standard. For more information about UNIMARC visit the [UNIMARC Strategic Programme](https://www.ifla.org/unimarc "UNIMARC Strategic Programme") page.
