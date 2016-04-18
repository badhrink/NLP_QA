import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class Parse {

	private static List<String> ACTOR_SYNONYMS = Arrays.asList("artist",
			"star", "character", "clown", "comedian", "entertainer",
			"performer", "player", "villain", "amateur", "barnstormer",
			"extra", "foil", "ham", "headliner", "idol", "impersonator",
			"lead", "mime", "mimic", "pantomimist", "play-actor", "soubrette",
			"stand-in", "stooge", "thespian", "trouper", "understudy",
			"ventriloquist", "walk-on", "bit player", "hambone", "ingenue",
			"straight person", "thesp");

	private static List<String> WIN_SYNONYMS = Arrays.asList("accomplish",
			"acquire", "actualize", "attain", "bring about", "bring off",
			"bring to pass", "cap", "carry out", "carry through", "close",
			"complete", "conclude", "consummate", "deliver", "discharge",
			"dispatch", "do", "earn", "earn wings", "effect", "effectuate",
			"enact", "end", "execute", "finish", "follow through", "fulfill",
			"gain", "get", "get done", "manage", "negotiate", "obtain",
			"perfect", "perform", "procure", "produce", "rack up", "reach",
			"realize", "resolve", "score", "seal", "see through", "settle",
			"sign", "solve", "win", "wind up", "work out");

	private static List<String> SING_SYNONYMS = Arrays.asList(
			"a carry voice with tune one's", "belt out", "chant", "croon",
			"hum", "intone", "serenade", "shout", "wait", "warble", "whistle",
			"buzz", "cantillate", "carol", "chirp", "choir", "descant",
			"groan", "harmonize", "hymn", "mouth", "pipe", "purr", "resound",
			"roar", "singsong", "solo", "trill", "troll", "tune", "vocalize",
			"whine", "yodel", "into burst song", "canary", "duet",
			"a lift voice up", "line out", "lullaby", "make melody");

	private static List<String> MOVIE_SYNONYMS = Arrays.asList("cine",
			"cinema", "cinematics", "cinematograph", "feature film", "film",
			"flick", "flicker", "movie", "picture moving", "photodrama",
			"photoplay", "picture", "picture show", "screen silver", "talkie",
			"talking picture", "videotape");

	private static List<String> GENRE_SYNONYMS = Arrays.asList("breed",
			"class", "description", "feather", "sort", "ilk", "kidney", "kind",
			"like", "manner", "nature", "order", "species", "strain", "stripe",
			"type", "variety", "model", "sample", "specimen", "bracket",
			"bunch", "category", "division", "family", "grade", "group",
			"grouping", "lot", "persuasion", "ranks", "set", "suite", "rank");

	private static List<String> UNIQUE_MOVIE_VERBS = Arrays.asList("direct",
			"win", "act", "directed", "directing", "acted", "acting", "won",
			"winning", "acts");

	private static List<String> UNIQUE_MUSIC_VERBS = Arrays.asList("sing",
			"sung", "sang", "sings");

	private static List<String> MUSIC_NOUNS = Arrays.asList("album", "genre",
			"track", "singer", "song");

	private static List<String> MOVIE_NOUNS = Arrays.asList("actor",
			"director", "movie", "oscar");

	private static List<String> GEO_NOUNS = Arrays.asList("border", "capital",
			"country", "continent", "seas", "mountain", "ocean");

	private static String CATEGORY_MOVIE = "Movie";

	private static String CATEGORY_MUSIC = "Music";

	private static String CATEGORY_GEOGRAPHY = "Geography";

	private static String CATEGORY_MUSIC_MOVIE = "Music||Movie";


	public static void main(String[] args) {
		parseQuestions("input.txt");
	}

	public static void parseQuestions(String filename) {
		List<String> tnerlist = null;

		try {
			File dir = new File(".");
			File fin = new File(dir.getCanonicalPath() + File.separator + "src"
					+ File.separator + filename);
			//File fin = new File(args[0]);
			List<String> nounTagsList = Arrays.asList("NN", "NNS", "NNP",
					"NNPS");
			List<String> verbTagsList = Arrays.asList("VB", "VBD", "VBG",
					"VBN", "VBP", "VBZ");
			List<String> qnTagsList = Arrays.asList("WDT", "WP", "WP$", "WRB");

			Properties props = new Properties();
			props.setProperty("annotators",
					"tokenize, ssplit, pos, lemma, ner, parse, depparse");
			StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

			BufferedReader br = new BufferedReader(new FileReader(fin));

			String sentence = null;
			Question question;
			while ((sentence = br.readLine()) != null) {
				if(sentence.trim().isEmpty()){
					continue;
				}
				tnerlist = new ArrayList<String>();
				Annotation document = new Annotation(sentence.trim());
				pipeline.annotate(document);

				question = new Question(sentence.trim());

				// Generate Parse Tree
				question.parseTree = new ArrayList<Tree>();
				question.nounList = new ArrayList<String>();
				question.verbList = new ArrayList<String>();
				List<CoreMap> sentences = document
						.get(SentencesAnnotation.class);
				for (CoreMap sent : sentences) {
					Tree tree = sent.get(TreeAnnotation.class);
					question.parseTree.add(tree);
					ArrayList<TaggedWord> taggedWordList = tree.taggedYield();
					for (TaggedWord tw : taggedWordList) {
						if (nounTagsList.contains(tw.tag())) {
							question.nounList.add(tw.value().toLowerCase());
						} else if (verbTagsList.contains(tw.tag())) {
							question.verbList.add(tw.value().toLowerCase());
						} else if (question.whQuestionType == null
								&& qnTagsList.contains(tw.tag())) {
							question.whQuestionType = tw.value().toLowerCase();
						}
					}
				}
				//System.out.println(sentence + "\n Parse Tree: "
					//	+ question.parseTree.toString() + "\n");

				// Perform Named Entity Recognition
				List<CoreLabel> tokens = document
						.get(CoreAnnotations.TokensAnnotation.class);
				List<String> resultNER = new ArrayList<String>();
				for (CoreLabel token : tokens) {
					// this is the text of the token
					String nerTag = token
							.get(CoreAnnotations.NamedEntityTagAnnotation.class);
					resultNER.add(nerTag);
					question.AddTokenNer(token.toString(), nerTag);
				}
				//System.out.println("NER: " + resultNER.toString());
				for (Question.TokenNER tn : question.tokenNerList) {
				//	System.out.println("Token:NER = " + tn.token.toString()
					//		+ " " + tn.ner.toString());
					tnerlist.add(tn.ner.toString());
					//System.out.print(tnerlist);
				}

				// Generate Dependency Parse Graph
				List<CoreMap> sentencesd = document
						.get(CoreAnnotations.SentencesAnnotation.class);
				CoreMap sentenced = (CoreMap) sentencesd.get(0);
				question.dependencyGraph = sentenced
						.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
				//System.out.println("Dependency Tree:\n"
					//	+ question.dependencyGraph.toString());
				question.dependencyRoot = question.dependencyGraph
						.getFirstRoot().word().toLowerCase();
				if (question.whQuestionType == null) {
					question.whQuestionType = question.dependencyRoot;
				}
				performUniqueQuestionClassification(question,tnerlist);
				question.printResult();

			}
			br.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}

	}
	//Function to perform Question Classification based on unique identifiers in the question
	private static void performUniqueQuestionClassification(Question question,List<String> tnerlist) {
		//Checks for unique verbs identifiers in the categories
		if (!Collections.disjoint(UNIQUE_MOVIE_VERBS, question.verbList)
				|| !Collections.disjoint(WIN_SYNONYMS, question.verbList)) {
			question.qnCategory = CATEGORY_MOVIE;
		} else if (!Collections.disjoint(UNIQUE_MUSIC_VERBS, question.verbList)
				|| !Collections.disjoint(SING_SYNONYMS, question.verbList)) {
			question.qnCategory = CATEGORY_MUSIC;
			//Checks for unique nouns identifiers in the categories
		} else if (!Collections.disjoint(MUSIC_NOUNS, question.nounList)) {
			question.qnCategory = CATEGORY_MUSIC;
		} else if (!Collections.disjoint(MOVIE_NOUNS, question.nounList)
				|| !Collections.disjoint(MOVIE_SYNONYMS, question.verbList)
				|| !Collections.disjoint(ACTOR_SYNONYMS, question.verbList)) {
			question.qnCategory = CATEGORY_MOVIE;
		} else if (!Collections.disjoint(GEO_NOUNS, question.nounList)) {

			question.qnCategory = CATEGORY_GEOGRAPHY;

		}
		// Performs Question Classification with the help of Named Entity Recognition
		else if ((!tnerlist.contains("PERSON"))
				&& tnerlist.contains("LOCATION")) {
			question.qnCategory = CATEGORY_GEOGRAPHY;
		}
		else {
			//Perform Question Classification with the help of Voters
			performAmbiguosQuestionClassification(question,tnerlist);
		}}
		//Function to perform Question Classification with the help of Voters
		private static void performAmbiguosQuestionClassification(Question question,List<String> tnerlist){
			String nerConfidence = performOtherNERValidation(question);
			String verbsConfidence = performOtherVerbsValidation(question);
			String nounsConfidence = performOtherNounsValidation(question);
			String rootConfidence = performRootsValidation(question);
			int countOfCategoryMusicMovie = getCountOfCategoryMusicMovie(
					nerConfidence, verbsConfidence, nounsConfidence,
					rootConfidence);
			//Perform classification based on what majority of voters think is the category
			int countOfMusic = (nounsConfidence.equals(CATEGORY_MUSIC)) ? 1 : 0;
			question.qnCategory = (countOfCategoryMusicMovie > countOfMusic) ? CATEGORY_MUSIC_MOVIE
					: CATEGORY_MUSIC;
		}

	//Named Entity Recognition Voters check for the condition and performs classification
	private static String performOtherNERValidation(Question question) {
		if (question.tokenNerList.contains("PERSON")
				|| question.tokenNerList.contains("LOCATION")) {
			return CATEGORY_MUSIC_MOVIE;
		}
		return null;
	}
	//Verbs Voters check for the condition and performs classification
	private static String performOtherVerbsValidation(Question question) {
		if (!Collections.disjoint(
				Arrays.asList("born", "release", "released", "releasing"),
				question.verbList)) {
			return CATEGORY_MUSIC_MOVIE;
		}
		return null;
	}
	//Nouns Voters check for the condition and performs classification
	private static String performOtherNounsValidation(Question question) {
		if (question.tokenNerList.contains("artist")) {
			return CATEGORY_MUSIC;
		} else {
			return CATEGORY_MUSIC_MOVIE;
		}
	}
	//Dependency parser Voters check for the condition and performs classification
	private static String performRootsValidation(Question question) {
		String output = null;
		if (Arrays.asList("where", "who", "which", "is", "was", "did", "does")
				.contains(question.dependencyRoot)) {
			return output;
		} else {
			if (Arrays.asList("born", "release", "released", "releasing")
					.contains(question.dependencyRoot)) {
				output = CATEGORY_MUSIC_MOVIE;
			}
		}
		return output;
	}

	private static int getCountOfCategoryMusicMovie(String nerConfidence,
			String verbsConfidence, String nounsConfidence,
			String rootConfidence) {
		int count = 0;
		if (nerConfidence != null) {
			count++;
		}
		if (verbsConfidence != null) {
			count++;
		}
		if (nounsConfidence != null) {
			count++;
		}
		if (rootConfidence != null) {
			count++;
		}
		return count;
	}

}
