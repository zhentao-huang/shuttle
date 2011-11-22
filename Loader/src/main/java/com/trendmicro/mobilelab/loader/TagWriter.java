package com.trendmicro.mobilelab.loader;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Stack;

public class TagWriter extends PrintWriter
{
    private class Cursor
    {
        TagWriter top;
        TagWriter last;
        Stack<TagWriter> stack;
        boolean finished;
    }
    
    public TagWriter(PrintWriter writer, String root)
    {
    	super(writer, true);
        mCursor = new Cursor();
        mCursor.top = this;
        mCursor.last = this;
        mCursor.finished = false;
        mCursor.stack = new Stack<TagWriter>();
        mParent = null;
        mAttrs = new HashMap<String, String>();
        mChildren = new ArrayList<Object>();
        mLeading = 0;
        mIndent = 0;
        setTag(root);
    }
    
    public TagWriter(PrintWriter writer)
    {
        this(writer, "html");
    }
    
    public TagWriter(TagWriter parent, String tag)
    {
        super(parent, true);
        mParent = parent;
        mCursor = parent.mCursor;
        mCursor.last = this;
        mAttrs = new HashMap<String, String>();
        mChildren = new ArrayList<Object>();
        mIndent = parent.mIndent;
        mLeading = parent.mLeading + mIndent;
        setTag(tag);
    }
    
    public TagWriter setTag(String tag)
    {
        mTag = tag;
        return this;
    }
    
    public TagWriter addAttr(String name, String value)
    {
        mAttrs.put(name,value);
        return this;
    }
    
    public TagWriter addChild(String tag)
    {
        TagWriter tagWriter = new TagWriter(this, tag);
        mChildren.add(tagWriter);
        return tagWriter;
    }
    
    public TagWriter addText(String text)
    {
        mChildren.add(text);
        return this;
    }
    
    private void writeBegin()
    {
        writeLeading();
        super.print("<" + mTag);
        for (Entry<String, String> entry : mAttrs.entrySet())
        {
            print(" " + entry.getKey() + "=\"" + entry.getValue() + "\"");
        }
        
        if (mChildren.isEmpty())
        {
            println("/>");
        }
        else
        {
            println(">");
        }
    }
    
    private void writeChildren()
    {
        if (!mChildren.isEmpty())
        {
            for (Object obj : mChildren)
            {
                if (obj instanceof String)
                {
                    writeLeading();
                    writeIndent();
                    println(obj);
                }
                else if (obj instanceof TagWriter)
                {
                    TagWriter tagWriter = (TagWriter) obj;
                    tagWriter.finish();
                }
            }
        }
    }
    
    private void writeEnd()
    {
        if (!mChildren.isEmpty())
        {
            writeLeading();
            println("</" + mTag + ">");
        }
    }
    
    private void writeLeading()
    {
        if (mLeadingStr == null)
        {
            mLeadingStr = new StringBuilder();
            for (int i = mLeading; i > 0; --i)
            {
                mLeadingStr.append(" ");
            }
        }
        print(mLeadingStr);
    }
    
    private void writeIndent()
    {
        if (mIndentStr == null)
        {
            mIndentStr = new StringBuilder();
            for (int i = mIndent; i > 0; --i)
            {
                mIndentStr.append(" ");
            }
        }
        print(mIndentStr);
    }
    
    public TagWriter end()
    {
        mCursor.last = mParent;
        return mParent;
    }
    
    public TagWriter top()
    {
        return mCursor.top;
    }
    
    public TagWriter last()
    {
        return mCursor.last;
    }
    
    public void finish()
    {
        if (mCursor.finished)
        {
            writeBegin();
            writeChildren();
            writeEnd();
            
            if (mCursor.top == this)
            {
                flush();
            }
        }
        else
        {
            mCursor.finished = true;
            mCursor.top.finish();
        }
    }
    
    public TagWriter setIndent(int indent)
    {
        mIndent = indent;
        return this;
    }
    
    public TagWriter push()
    {
    	mCursor.stack.push(mCursor.last);
    	return mCursor.last;
    }
    
    public TagWriter pop()
    {
    	mCursor.last = mCursor.stack.pop();
    	return mCursor.last;
    }
    
    public static void main(String[] args)
    {
        System.out.println("Begine writer");
        TagWriter html = new TagWriter(new PrintWriter(System.out));
        html.setIndent(2)
            .addChild("head")
                .addChild("title")
                    .addText("Main view of Trend Box").end()
                .end()
            .addChild("body").addAttr("id","bodyid").addAttr("class","classbody")
                .addChild("B")
                    .addText("Hello world for Trend Box!").end()
                .addText("The second line")
                .addChild("br").end();
        html.last()
            .addChild("table")
                .addChild("tr")
                    .addChild("th")
                        .addText("Key").end()
                    .addChild("th")
                        .addText("Value").end()
                    .end()
                .addChild("tr")
                    .addChild("td").addText("First").end()
                    .addChild("td").addText("#1").finish();
    }
    
    private Cursor mCursor;
    
    private String mTag;
    private HashMap<String, String> mAttrs;
    private ArrayList<Object> mChildren;
    private int mIndent;
    private int mLeading;
    private StringBuilder mLeadingStr;
    private StringBuilder mIndentStr;

    private TagWriter mParent;
}
