package ixa.srl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import org.jdom2.JDOMException;

import ixa.kaflib.KAFDocument;

public class SRL {

	public static void main(String[] args) { // throws IOException {

		try {
			Annotate annotator = new Annotate();
			// Input
			BufferedReader stdInReader = null;
			// Output
			BufferedWriter w = null;

			stdInReader = new BufferedReader(new InputStreamReader(System.in,
					"UTF-8"));
			// stdInReader = new BufferedReader(new InputStreamReader(new
			// FileInputStream("models/eng/ner.kaf"),Charset.forName("UTF-8")));
			// stdInReader = new BufferedReader(new InputStreamReader(new
			// FileInputStream("models/eng/nerwithdeps.kaf"),Charset.forName("UTF-8")));

			String lang = "eng";
			if (args[0].equals("en")) {
				lang = "eng";
			} else if (args[0].equals("es")) {
				lang = "spa";
			}

			w = new BufferedWriter(new OutputStreamWriter(System.out, "UTF-8"));
			KAFDocument kaf = KAFDocument.createFromStream(stdInReader);
			if (args.length == 2) {
				annotator.SRLToKAF(kaf, lang, args[1]);
			} else {
				annotator.SRLToKAF(kaf, lang, "");
			}

			w.write(kaf.toString());
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}