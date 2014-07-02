package cc.hughes.droidchatty2.text;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import android.graphics.Typeface;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

public class TagParser implements ContentHandler {

    private static final String TAG = "TagParser";
    private static final String SOURCE = "HTML_SOURCE";

	SpannableStringBuilder mSpannableStringBuilder;
    boolean mMultiline;
	
	private TagParser(boolean multiline) {
		mSpannableStringBuilder = new SpannableStringBuilder();
        mMultiline = multiline;
	}

    public static Spanned fromHtml(String source) {
        return fromHtml(source, true);
    }

	public static Spanned fromHtml(String source, boolean multiline) {
		
		TagParser tagParser = new TagParser(multiline);
		
		try {
            XMLReader parser = new org.ccil.cowan.tagsoup121.Parser();
		
			parser.setContentHandler(tagParser);
			parser.parse(new InputSource(new StringReader(source)));

			return tagParser.mSpannableStringBuilder;
			
		} catch (Exception e) {
            Crashlytics.logException(e);
		}

        try {
		    // fall back to basic HTML parser!
		    return Html.fromHtml(source);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing tags with Html.", e);
        }

        // okay, just show the whole damn HTML I guess
        return new SpannableString(source);
	}
	
	void handleStartTag(String tag, Attributes attributes) {
		if (tag.equalsIgnoreCase("br")) {
			// skip, handle </br> instead
		} else if (tag.equalsIgnoreCase("p")) {
			handleP(mSpannableStringBuilder);
		} else if (tag.equalsIgnoreCase("b")) {
			start(mSpannableStringBuilder, new Bold());
		} else if (tag.equalsIgnoreCase("i")) {
			start(mSpannableStringBuilder, new Italic());
		} else if (tag.equalsIgnoreCase("a")) {
			startA(mSpannableStringBuilder, attributes);
		} else if (tag.equalsIgnoreCase("span")) {
			String c = attributes.getValue("", "class");
			start(mSpannableStringBuilder, new Span(c));
		}
	}
	
	void handleEndTag(String tag) {
		if (tag.equalsIgnoreCase("br")) {
			handleBr(mSpannableStringBuilder);
		} else if (tag.equalsIgnoreCase("p")) {
			handleP(mSpannableStringBuilder);
		} else if (tag.equalsIgnoreCase("b")) {
			end(mSpannableStringBuilder, Bold.class, new StyleSpan(Typeface.BOLD));
		} else if (tag.equalsIgnoreCase("i")) {
			end(mSpannableStringBuilder, Italic.class, new StyleSpan(Typeface.ITALIC));
		} else if (tag.equalsIgnoreCase("a")) {
			endA(mSpannableStringBuilder);
		} else if (tag.equalsIgnoreCase("span")) {
			endLastSpan(mSpannableStringBuilder);
		}
	}
	
	static void start(SpannableStringBuilder text, Object mark) {
		int len = text.length();
		text.setSpan(mark, len, len, Spannable.SPAN_MARK_MARK);
	}
	
	static void end(SpannableStringBuilder text, Class kind, Object repl) {
		int len = text.length();
		Object obj = getLast(text, kind);
		int where = text.getSpanStart(obj);
		
		text.removeSpan(obj);
		
		if (where != len) {
			text.setSpan(repl, where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
	}
	
	static void endA(SpannableStringBuilder text) {
		int len = text.length();
		Object obj = getLast(text, Href.class);
		int where = text.getSpanStart(obj);

		text.removeSpan(obj);

		if (where != len) {
			Href h = (Href) obj;

			if (h.mHref != null) {
				text.setSpan(new InternalURLSpan(h.mHref), where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
	}
	
	static void endLastSpan(SpannableStringBuilder text) {
		int len = text.length();
		Object obj = getLast(text, Span.class);
		int where = text.getSpanStart(obj);
		
		text.removeSpan(obj);
		
		if (where != len) {
			Span span = (Span)obj;
			
			if (span.mClass != null) {
				CharacterStyle style = getSpanStyle(span.mClass);
				if (style != null) {
					text.setSpan(style, where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
		}
	}
	
	static CharacterStyle getSpanStyle(String className) {
		if (COLORS.containsKey(className)) {
			return new ForegroundColorSpan(COLORS.get(className) | 0xFF000000);
		} else if (className.equalsIgnoreCase("jt_quote")) {
			return new TypefaceSpan("serif");
		} else if (className.equalsIgnoreCase("jt_bold")) {
			return new StyleSpan(Typeface.BOLD);
		} else if (className.equalsIgnoreCase("jt_italic")) {
			return new StyleSpan(Typeface.ITALIC);
		} else if (className.equalsIgnoreCase("jt_underline")) {
			return new UnderlineSpan();
		} else if (className.equalsIgnoreCase("jt_strike")) {
			return new StrikethroughSpan();
		} else if (className.equalsIgnoreCase("jt_spoiler")) {
			return new SpoilerSpan();
		} else if (className.equalsIgnoreCase("jt_sample")) {
			return new RelativeSizeSpan((float)0.80);
		} else if (className.equalsIgnoreCase("jt_code")) {
			return new TypefaceSpan("monospace");
		}
		
		return null;
	}

	static Object getLast(Spanned text, Class kind) {
		Object[] objs = text.getSpans(0,  text.length(), kind);
		
		if (objs.length == 0) {
			return null;
		} else {
			return objs[objs.length - 1];
		}
	}
	
	static void startA(SpannableStringBuilder text, Attributes attributes) {
		String href = attributes.getValue("", "href");
		
		// fix for shacknews posted links
		if (!href.startsWith("http"))
			href = "http://www.shacknews.com" + href;
		
		int len = text.length();
		text.setSpan(new Href(href), len, len, Spannable.SPAN_MARK_MARK);
	}
	
	void handleBr(SpannableStringBuilder text) {
        if (mMultiline)
		    text.append("\n");
	}
	
	void handleP(SpannableStringBuilder text) {
        if (mMultiline) {
            int len = text.length();

            if (len >= 1 && text.charAt(len - 1) == '\n') {
                if (len >= 2 && text.charAt(len - 2) == '\n') {
                    return;
                }

                text.append("\n");
                return;
            }

            if (len != 0) {
                text.append("\n\n");
            }
        }
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		StringBuilder sb = new StringBuilder();

		/*
		 * Ignore whitespace that immediately follows other whitespace;
		 * newlines count as spaces.
		 */
		
		for (int i = 0; i < length; i++) {
			char c = ch[i + start];

			if (c == ' ' || c == '\n') {
				char pred;
				int len = sb.length();

				if (len == 0) {
					len = mSpannableStringBuilder.length();

					if (len == 0) {
						pred = '\n';
					} else {
						pred = mSpannableStringBuilder.charAt(len - 1);
					}
				} else {
					pred = sb.charAt(len - 1);
				}

				if (pred != ' ' && pred != '\n') {
					sb.append(' ');
				}
			} else {
				sb.append(c);
			}
		}
		
		mSpannableStringBuilder.append(sb);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		handleStartTag(localName, atts);
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		handleEndTag(localName);
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException { }

	@Override
	public void processingInstruction(String target, String data) throws SAXException { }

	@Override
	public void setDocumentLocator(Locator locator) { }

	@Override
	public void skippedEntity(String name) throws SAXException { }

	@Override
	public void startDocument() throws SAXException { }

	@Override
	public void endDocument() throws SAXException {	}

	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException { }
	
	@Override
	public void endPrefixMapping(String prefix) throws SAXException { }

	private static class Span {
		public String mClass;
		public Span(String className) {
			mClass = className;
		}
	}
	private static class Bold { }
	private static class Italic { }
	
	private static class Href {
		public String mHref;
		public Href(String href) {
			mHref = href;
		}
	}
	
	static HashMap<String, Integer> COLORS = buildColorMap();

	static HashMap<String, Integer> buildColorMap() {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.put("jt_red", 0xFF0000);
		map.put("jt_green", 0x8DC63F);
		map.put("jt_pink", 0xF49AC1);
		map.put("jt_olive", 0x808000);
		map.put("jt_fuchsia", 0xC0FFC0);
		map.put("jt_yellow", 0xFFDE00);
		map.put("jt_blue", 0x44AEDF);
		map.put("jt_lime", 0xC0FFC0);
		map.put("jt_orange", 0xF7941C);
        map.put("jt_wtf242", 0x808080);
		return map;
	}
	
}
