package ixa.srl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class PredicateMatrix {

        private static final Pattern JARPATH_PATTERN_BEGIN = Pattern.compile("file:");
	private static final Pattern JARPATH_PATTERN_END = Pattern.compile("[^/]+jar!.+");

	private HashMap<String, ArrayList<String>> vnClass = new HashMap<String, ArrayList<String>>();
	private HashMap<String, ArrayList<String>> vnSubClass = new HashMap<String, ArrayList<String>>();
	private HashMap<String, ArrayList<String>> fnFrame = new HashMap<String, ArrayList<String>>();
	private HashMap<String, ArrayList<String>> eventType = new HashMap<String, ArrayList<String>>();

	private HashMap<String, ArrayList<String>> vnThematicRole = new HashMap<String, ArrayList<String>>();
	private HashMap<String, ArrayList<String>> fnFrameElement = new HashMap<String, ArrayList<String>>();

	public PredicateMatrix() {
		try {
		        String jarpath = this.getClass().getResource("").getPath();
		        Matcher matcher = JARPATH_PATTERN_BEGIN.matcher(jarpath);
		        jarpath = matcher.replaceAll("");		
		        matcher = JARPATH_PATTERN_END.matcher(jarpath);
		        jarpath = matcher.replaceAll("");

			BufferedReader pmReader = new BufferedReader(new InputStreamReader(new FileInputStream(jarpath + "PredicateMatrix/PredicateMatrix.txt"),Charset.forName("UTF-8")));

			String pmLine;
			String[] pmFields;

			while ((pmLine = pmReader.readLine()) != null) {
				pmFields = pmLine.split("\t");
				if (!pmFields[10].equals("NULL")) {
					if (!pmFields[0].equals("NULL")) {
						ArrayList<String> array = new ArrayList<String>();
						if (vnClass.containsKey(pmFields[10]))
							array = vnClass.get(pmFields[10]);
						if (newElement(array, pmFields[0]))
							array.add(pmFields[0]);
						vnClass.put(pmFields[10], array);
					}
					if (!pmFields[2].equals("NULL")) {
						ArrayList<String> array = new ArrayList<String>();
						if (vnSubClass.containsKey(pmFields[10]))
							array = vnSubClass.get(pmFields[10]);
						if (newElement(array, pmFields[2]))
							array.add(pmFields[2]);
						vnSubClass.put(pmFields[10], array);
					}
					if (!pmFields[7].equals("NULL")) {
						ArrayList<String> array = new ArrayList<String>();
						if (fnFrame.containsKey(pmFields[10]))
							array = fnFrame.get(pmFields[10]);
						if (newElement(array, pmFields[7]))
							array.add(pmFields[7]);
						fnFrame.put(pmFields[10], array);
					}
					if (!pmFields[21].equals("NULL")) {
						ArrayList<String> array = new ArrayList<String>();
						if (eventType.containsKey(pmFields[10]))
							array = eventType.get(pmFields[10]);
						if (newElement(array, pmFields[21]))
							array.add(pmFields[21]);
						eventType.put(pmFields[10], array);
					}
					if (!pmFields[11].equals("NULL")) {
						if (!pmFields[6].equals("NULL")) {
							ArrayList<String> array = new ArrayList<String>();
							if (vnThematicRole.containsKey(pmFields[10] + ":A"
									+ pmFields[11]))
								array = vnThematicRole.get(pmFields[10] + ":A"
										+ pmFields[11]);
							if (newElement(array, pmFields[0] + "#"
									+ pmFields[6]))
								array.add(pmFields[0] + "#" + pmFields[6]);
							vnThematicRole.put(pmFields[10] + ":A"
									+ pmFields[11], array);
						}
						if (!pmFields[9].equals("NULL")) {
							ArrayList<String> array = new ArrayList<String>();
							if (fnFrameElement.containsKey(pmFields[10] + ":A"
									+ pmFields[11]))
								array = fnFrameElement.get(pmFields[10] + ":A"
										+ pmFields[11]);
							if (newElement(array, pmFields[7] + "#"
									+ pmFields[9]))
								array.add(pmFields[7] + "#" + pmFields[9]);
							fnFrameElement.put(pmFields[10] + ":A"
									+ pmFields[11], array);
						}
					}
				}
			}

			pmReader.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<String> getVNClasses(String PBSense) {
		ArrayList<String> array = new ArrayList<String>();
		if (vnClass.containsKey(PBSense))
			array = vnClass.get(PBSense);
		return array;
	}

	public ArrayList<String> getVNSubClasses(String PBSense) {
		ArrayList<String> array = new ArrayList<String>();
		if (vnSubClass.containsKey(PBSense))
			array = vnSubClass.get(PBSense);
		return array;
	}

	public ArrayList<String> getFNFrames(String PBSense) {
		ArrayList<String> array = new ArrayList<String>();
		if (fnFrame.containsKey(PBSense))
			array = fnFrame.get(PBSense);
		return array;
	}

	public ArrayList<String> getEventTypes(String PBSense) {
		ArrayList<String> array = new ArrayList<String>();
		if (eventType.containsKey(PBSense))
			array = eventType.get(PBSense);
		return array;
	}

	public ArrayList<String> getVNThematicRoles(String PBSenseArgument) {
		ArrayList<String> array = new ArrayList<String>();
		if (vnThematicRole.containsKey(PBSenseArgument))
			array = vnThematicRole.get(PBSenseArgument);
		return array;
	}

	public ArrayList<String> getFNFrameElements(String PBSenseArgument) {
		ArrayList<String> array = new ArrayList<String>();
		if (fnFrameElement.containsKey(PBSenseArgument))
			array = fnFrameElement.get(PBSenseArgument);
		return array;
	}

	private boolean newElement(ArrayList<String> array, String element) {
		boolean isNew = true;
		for (int i = 0; i < array.size(); i++)
			if (array.get(i).toString().equals(element))
				isNew = false;
		return isNew;
	}
}
