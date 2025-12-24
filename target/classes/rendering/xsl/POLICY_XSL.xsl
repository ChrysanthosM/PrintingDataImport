<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format">

    <xsl:output method="xml" indent="yes"/>

    <!-- Root template -->
    <xsl:template match="/invoice">
        <fo:root>
            <fo:layout-master-set>
                <fo:simple-page-master master-name="A4"
                    page-height="29.7cm" page-width="21cm" margin="2cm">
                    <fo:region-body/>
                </fo:simple-page-master>
            </fo:layout-master-set>

            <fo:page-sequence master-reference="A4">
                <fo:flow flow-name="xsl-region-body">

                    <!-- Header -->
                    <fo:block font-size="14pt" font-weight="bold">
                        <xsl:value-of select="header/company"/>
                    </fo:block>
                    <fo:block>Date: <xsl:value-of select="header/date"/></fo:block>
                    <fo:block>Invoice #: <xsl:value-of select="header/invoiceNumber"/></fo:block>
                    <fo:block linefeed-treatment="preserve">&#x0A;</fo:block>

                    <!-- Items Table -->
                    <fo:table border="1pt solid black" table-layout="fixed" width="100%">
                        <fo:table-column column-width="5cm"/>
                        <fo:table-column column-width="3cm"/>
                        <fo:table-column column-width="3cm"/>
                        <fo:table-body>
                            <fo:table-row>
                                <fo:table-cell><fo:block>Description</fo:block></fo:table-cell>
                                <fo:table-cell><fo:block>Quantity</fo:block></fo:table-cell>
                                <fo:table-cell><fo:block>Price</fo:block></fo:table-cell>
                            </fo:table-row>
                            <xsl:for-each select="items/item">
                                <fo:table-row>
                                    <fo:table-cell><fo:block><xsl:value-of select="description"/></fo:block></fo:table-cell>
                                    <fo:table-cell><fo:block><xsl:value-of select="quantity"/></fo:block></fo:table-cell>
                                    <fo:table-cell><fo:block><xsl:value-of select="price"/></fo:block></fo:table-cell>
                                </fo:table-row>
                            </xsl:for-each>
                        </fo:table-body>
                    </fo:table>

                    <!-- Footer -->
                    <fo:block font-weight="bold" margin-top="1cm">
                        Total: <xsl:value-of select="footer/total"/>
                    </fo:block>

                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>
</xsl:stylesheet>