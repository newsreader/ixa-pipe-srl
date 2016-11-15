package ixa.srl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import se.lth.cs.srl.Parse;
import se.lth.cs.srl.SemanticRoleLabeler;
import se.lth.cs.srl.languages.Language;
import se.lth.cs.srl.options.CompletePipelineCMDLineOptions;
import se.lth.cs.srl.options.FullPipelineOptions;
import se.lth.cs.srl.pipeline.Pipeline;
import se.lth.cs.srl.pipeline.Reranker;
import se.lth.cs.srl.pipeline.Step;
import se.lth.cs.srl.preprocessor.Preprocessor;

public class PreProcess {

	private static final Pattern JARPATH_PATTERN_BEGIN = Pattern.compile("file:");
	private static final Pattern JARPATH_PATTERN_END = Pattern.compile("[^/]+jar!.+");

	public Preprocessor pp;
	public SemanticRoleLabeler srl;
	public CompletePipelineCMDLineOptions options;
	
	private void getCompletePipeline(FullPipelineOptions options,
			String option) throws ZipException, IOException,
			ClassNotFoundException {

		Preprocessor pp = Language.getLanguage().getPreprocessor(options);
		Parse.parseOptions = options.getParseOptions();

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
			this.pp = pp;
			this.srl = srl;
		} else {
			this.pp = pp;
		}
	}
	
	
	public PreProcess(String lang, String option)
			throws Exception {

		String jarpath = this.getClass().getResource("").getPath();
		Matcher matcher = JARPATH_PATTERN_BEGIN.matcher(jarpath);
		jarpath = matcher.replaceAll("");		
		matcher = JARPATH_PATTERN_END.matcher(jarpath);
		jarpath = matcher.replaceAll("");

		String[] models = new String[3];
		BufferedReader modelsFile = new BufferedReader(new InputStreamReader(new FileInputStream(jarpath + "models.cnf"),Charset.forName("UTF-8")));
		String modelsLine;
		String[] modelsFields;
		while ((modelsLine = modelsFile.readLine()) != null) {
			modelsFields = modelsLine.split("\t");
			if (modelsFields[0].equals(lang)){
				if (modelsFields[1].equals("deps")) {
					models[0] = modelsFields[2]; 
				} else if (modelsFields[1].equals("srl")) {
					models[1] = modelsFields[2];
				} else if (modelsFields[1].equals("morph")) {
					models[2] = modelsFields[2];
				}
			}
		}
		modelsFile.close();

		String[] arguments = null;
		if (option.equals("only-deps")) {
			if (lang.equals("eng")) {
				arguments = new String[3];
				arguments[0] = lang;
				arguments[1] = "-parser";
				arguments[2] = models[0];
			} else if (lang.equals("spa")) {
				arguments = new String[5];
				arguments[0] = lang;
				arguments[1] = "-parser";
				arguments[2] = models[0];
				arguments[3] = "-morph";
				arguments[4] = models[2];
			}
		} else if (option.equals("only-srl")) {
			if (lang.equals("eng")) {
				arguments = new String[3];
				arguments[0] = lang;
				arguments[1] = "-srl";
				arguments[2] = models[1];
			} else if (lang.equals("spa")) {
				arguments = new String[5];
				arguments[0] = lang;
				arguments[1] = "-srl";
				arguments[2] = models[1];
				arguments[3] = "-morph";
				arguments[4] = models[2];
			}
		} else {
			if (lang.equals("eng")) {
				arguments = new String[5];
				arguments[0] = lang;
				arguments[1] = "-parser";
				arguments[2] = models[0];
				arguments[3] = "-srl";
				arguments[4] = models[1];
			} else if (lang.equals("spa")) {
				arguments = new String[7];
				arguments[0] = lang;
				arguments[1] = "-parser";
				arguments[2] = models[0];
				arguments[3] = "-srl";
				arguments[4] = models[1];
				arguments[5] = "-morph";
				arguments[6] = models[2];
			}
		}

		
		// String
		// error=FileExistenceVerifier.verifyCompletePipelineAllNecessaryModelFiles(options);
		// if(error!=null){
		// System.err.println(error);
		// System.err.println();
		// System.err.println("Aborting.");
		// System.exit(1);
		// }

		this.options = new CompletePipelineCMDLineOptions();
		this.options.parseCmdLineArgs(arguments);
		this.getCompletePipeline(this.options, option);
	}
}
