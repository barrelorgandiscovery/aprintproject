<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:param name="language" />

	<xsl:output method="html" encoding="UTF-8" />

	<xsl:template match="page">
		<html>
			<xsl:apply-templates />
		</html>
	</xsl:template>

	<xsl:template match="*">
		<xsl:choose>
			<xsl:when test="@xml:lang = $language ">
				<xsl:copy>
					<xsl:copy-of
						select="attribute::*[name()!='xml:lang']" />
					<xsl:apply-templates />
				</xsl:copy>
			</xsl:when>

			<xsl:when test="@xml:lang"></xsl:when>

			<xsl:otherwise>
				<xsl:copy>
					<xsl:copy-of select="@*"></xsl:copy-of>
					<xsl:apply-templates />
				</xsl:copy>
			</xsl:otherwise>

		</xsl:choose>

	</xsl:template>

</xsl:stylesheet>


