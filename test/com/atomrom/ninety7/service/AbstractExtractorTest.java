package com.atomrom.ninety7.service;

import java.io.IOException;

import com.atomrom.ninety7.service.search.Finder;
import com.atomrom.ninety7.service.util.TextAnalyzer;
import com.atomrom.ninety7.service.util.TextUtil;

public class AbstractExtractorTest {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		doTest("http://index.hu/belfold/2014/09/16/tolgyessy_a_fidesz_a_legeny_a_gaton/",
				"belföld, elemzés, fidesz, orbán viktor, tölgyessy péter");

		doTest("http://444.hu",
				"portugália, futball, cristiano ronaldo, cr7, c. ronaldo");

		doTest("http://index.hu/kultur/klassz/artista0811/",
				"THÜRINGER, hogy, magyar, nélkül, ÓRÁJA");

		doTest("http://eletestudomany.hu/horkolas_es_legzeskimaradas",
				"hosszú, éjszaka, rövid, nappal, tudomány, éjszaka");

		doTest("http://www.magyarkurir.hu/hirek/72-ora-kompromisszumok-nelkul-iden-is-varjak-fiatalokat-az-onkentes-szolgalatra",
				"hogy lehet magyar nélkül ÓRÁJA");

	}

	private static void doTest(String url, String queryWords)
			throws IOException {
		System.out.println("url:" + url);

		TextAnalyzer ta = new TextAnalyzer(url);
		System.out.println("abs:\n"
				+ ta.extractAbstract(TextUtil
						.commaSeparatedListToSet(queryWords, Finder.MIN_QUERY_WORD_LENGTH)));

		System.out.println();
	}
}
