package ixa.srl;

import is2.data.SentenceData09;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import se.lth.cs.srl.SemanticRoleLabeler;
import se.lth.cs.srl.Parse;
import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;
import se.lth.cs.srl.languages.Language;
import se.lth.cs.srl.options.CompletePipelineCMDLineOptions;
import se.lth.cs.srl.options.FullPipelineOptions;
import se.lth.cs.srl.pipeline.Pipeline;
import se.lth.cs.srl.pipeline.Reranker;
import se.lth.cs.srl.pipeline.Step;
import se.lth.cs.srl.preprocessor.Preprocessor;

public class MatePipeline {

	private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

	private Preprocessor pp;
	private SemanticRoleLabeler srl;

	public static MatePipeline getCompletePipeline(FullPipelineOptions options,
			String option) throws ZipException, IOException,
			ClassNotFoundException {

		Preprocessor pp = Language.getLanguage().getPreprocessor(options);
		Parse.parseOptions = options.getParseOptions();

		MatePipeline pipeline;
		if (!option.equals("only-deps")) {
			SemanticRoleLabeler srl;
			if (options.reranker) {
				srl = new Reranker(Parse.parseOptions);
			} else {
				ZipFile zipFile = new ZipFile(Parse.parseOptions.modelFile);
				if (Parse.parseOptions.skipPI) {
					srl = Pipeline.fromZipFile(zipFile, new Step[] { Step.pd,
							Step.ai, Step.ac });
				} else {
					srl = Pipeline.fromZipFile(zipFile);
				}
				zipFile.close();
			}
			pipeline = new MatePipeline(pp, srl);
		} else {
			pipeline = new MatePipeline(pp);
		}
		return pipeline;
	}

	public MatePipeline() {
	}

	private MatePipeline(Preprocessor preprocessor, SemanticRoleLabeler srl) {
		this.pp = preprocessor;
		this.srl = srl;
	}

	private MatePipeline(Preprocessor preprocessor) {
		this.pp = preprocessor;
	}

	public Sentence parse(List<String> lines, String option) throws Exception {

		String[] words = new String[lines.size()];
		String[] lemmas = new String[lines.size()];
		String[] tags = new String[lines.size()];
		String[] morphs = new String[lines.size()];
		int[] heads = new int[lines.size() - 1];
		String[] deprels = new String[lines.size() - 1];
		int idx = 0;
		for (String line : lines) {
			if (line.equals("<root>")) {
				words[idx] = "<root>";
				lemmas[idx] = "<root-LEMMA>";
				tags[idx] = "<root-POS>";
				morphs[idx] = "<root-FEAT>";
			} else {
				String[] tokens = WHITESPACE_PATTERN.split(line);
				words[idx] = tokens[1];
				lemmas[idx] = tokens[2];
				tags[idx] = tokens[4];
				morphs[idx] = tokens[6];

				if (option.equals("only-srl")) {
					heads[idx - 1] = Integer.parseInt(tokens[8]);
					deprels[idx - 1] = tokens[10];
				}
			}
			idx++;
		}

		Sentence s;

		if (option.equals("only-srl")) {
			s = new Sentence(words, lemmas, tags, morphs);
			s.setHeadsAndDeprels(heads, deprels);
			s.buildDependencyTree();
		} else {
			SentenceData09 instance = new SentenceData09();
			instance.init(words);
			instance.setLemmas(lemmas);
			instance.setPPos(tags);
			instance.setFeats(morphs);
			s = new Sentence(pp.preprocess(instance));
		}

		if (!option.equals("only-deps")) {
			srl.parseSentence(s);
		}

		return s;
	}

	public Sentence parseOraclePI(List<String> words, List<Boolean> isPred)
			throws Exception {
		Sentence s = new Sentence(pp.preprocess(words.toArray(new String[words
				.size()])));
		for (int i = 0; i < isPred.size(); ++i) {
			if (isPred.get(i)) {
				s.makePredicate(i);
			}
		}
		srl.parseSentence(s);
		return s;
	}

	public Document Pipeline(List<String> annotation, String lang, String option)
			throws Exception {

		Document doc = null;

		String jarpath = this.getClass().getClassLoader().getResource("")
				.getPath();
		String[] models = new String[2];
		if (lang.equals("eng")) {
			models[0] = jarpath
					+ "/models/eng/CoNLL2009-ST-English-ALL.anna-3.3.parser.model";
			models[1] = jarpath + "/models/eng/srl-eng.model";
		} else if (lang.equals("spa")) {
			models[0] = jarpath
					+ "/models/spa/CoNLL2009-ST-Spanish-ALL.anna-3.3.parser.model";
			models[1] = jarpath + "/models/spa/srl-spa.model";
		}

		String[] arguments;
		if (option.equals("only-deps")) {
			arguments = new String[3];
			arguments[0] = lang;
			arguments[1] = "-parser";
			arguments[2] = models[0];
		} else if (option.equals("only-srl")) {
			arguments = new String[3];
			arguments[0] = lang;
			arguments[1] = "-srl";
			arguments[2] = models[1];
		} else {
			arguments = new String[5];
			arguments[0] = lang;
			;
			arguments[1] = "-parser";
			arguments[2] = models[0];
			arguments[3] = "-srl";
			arguments[4] = models[1];
		}

		CompletePipelineCMDLineOptions options = new CompletePipelineCMDLineOptions();
		options.parseCmdLineArgs(arguments);
		// String
		// error=FileExistenceVerifier.verifyCompletePipelineAllNecessaryModelFiles(options);
		// if(error!=null){
		// System.err.println(error);
		// System.err.println();
		// System.err.println("Aborting.");
		// System.exit(1);
		// }

		MatePipeline pipeline = getCompletePipeline(options, option);
		doc = parseCoNLL09(options, option, pipeline, annotation);

		return doc;
	}

	private static Document parseCoNLL09(
			CompletePipelineCMDLineOptions options, String option,
			MatePipeline pipeline, List<String> in) throws IOException,
			Exception {

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();

		Document doc = db.newDocument();

		Element ROOT = doc.createElement("ROOT");
		doc.appendChild(ROOT);
		Element DEPS = doc.createElement("DEPS");
		ROOT.appendChild(DEPS);
		Element SRL = doc.createElement("SRL");
		ROOT.appendChild(SRL);

		List<String> forms = new ArrayList<String>();
		forms.add("<root>");
		List<Boolean> isPred = new ArrayList<Boolean>();
		isPred.add(false);
		String str;
		int senCount = 0;

		int sidx = 1;
		Iterator<String> iter = in.iterator();
		while (iter.hasNext()) {
			str = iter.next();
			if (str.trim().equals("")) {
				Sentence s;
				s = pipeline.parse(forms, option);
				String tag;
				for (int i = 1; i < s.size(); ++i) {
					Word w = s.get(i);

					Element d = doc.createElement("DEP");
					d.setAttribute("sentidx", String.valueOf(sidx));
					d.setAttribute("toidx", String.valueOf(i));
					d.setAttribute("fromidx", String.valueOf(w.getHeadId()));
					d.setAttribute("rfunc", w.getDeprel().toString());
					DEPS.appendChild(d);

					List<Predicate> predicates = s.getPredicates();
					for (int j = 0; j < predicates.size(); ++j) {
						Predicate pred = predicates.get(j);
						tag = pred.getArgumentTag(w);
						if (tag != null) {
							Element p = null;
							NodeList predList = doc
									.getElementsByTagName("PRED");
							for (int temp = 0; temp < predList.getLength(); temp++) {
								Node predNode = predList.item(temp);
								Element pElement = (Element) predNode;
								if (pElement.getAttribute("sentidx").equals(
										String.valueOf(String.valueOf(sidx)))
										&& pElement.getAttribute("predidx")
												.equals(String.valueOf(pred
														.getIdx()))) {
									p = pElement;
								}
							}

							if (p == null) {
								p = doc.createElement("PRED");
								p.setAttribute("sentidx", String.valueOf(sidx));
								p.setAttribute("predidx",
										String.valueOf(pred.getIdx()));
								p.setAttribute("sense", pred.getSense());
								SRL.appendChild(p);
							}

							Element n = doc.createElement("ARG");
							n.setAttribute("argument", tag);
							n.setAttribute("filleridx", String.valueOf(i));
							p.appendChild(n);
						}
					}
				}

				sidx++;

				forms.clear();
				forms.add("<root>");
				isPred.clear();
				isPred.add(false); // Root is not a predicate
				senCount++;
				if (senCount % 100 == 0) { // TODO fix output in general, don't
											// print to System.out. Wrap a
											// printstream in some (static)
											// class, and allow people to adjust
											// this. While doing this, also add
											// the option to make the output
											// file be -, ie so it prints to
											// stdout. All kinds of errors
											// should goto stderr, and nothing
											// should be printed to stdout by
											// default
					System.out.println("Processing sentence " + senCount);
				}
			} else {
				String[] tokens = WHITESPACE_PATTERN.split(str);
				forms.add(str);
				if (options.skipPI)
					isPred.add(tokens[12].equals("Y"));
			}
		}

		return doc;
	}

}
