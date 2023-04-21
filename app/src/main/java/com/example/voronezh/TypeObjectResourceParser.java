package com.example.voronezh;

import org.xmlpull.v1.XmlPullParser;
import java.util.ArrayList;

public class TypeObjectResourceParser {

    private ArrayList<TypeObject> typeObjects;

    public TypeObjectResourceParser(){
        typeObjects = new ArrayList<>();
    }

    public ArrayList<TypeObject> getTypeObjects(){
        return  typeObjects;
    }

    public boolean parse(XmlPullParser xpp){
        boolean status = true;
        TypeObject currentTypeObject = null;
        boolean inEntry = false;
        String textValue = "";

        try{
            int eventType = xpp.getEventType();
            while(eventType != XmlPullParser.END_DOCUMENT){

                String tagName = xpp.getName();
                switch (eventType){
                    case XmlPullParser.START_TAG:
                        if("type".equalsIgnoreCase(tagName)){
                            inEntry = true;
                            currentTypeObject = new TypeObject();
                        }
                        break;
                    case XmlPullParser.TEXT:
                        textValue = xpp.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        if(inEntry){
                            if("type".equalsIgnoreCase(tagName)){
                                typeObjects.add(currentTypeObject);
                                inEntry = false;
                            } else if("nameType".equalsIgnoreCase(tagName)){
                                currentTypeObject.setName(textValue);
                            } else if("idType".equalsIgnoreCase(tagName)){
                                currentTypeObject.setIdType(Integer.valueOf(textValue));
                            } else if("imgResource".equalsIgnoreCase(tagName)){
                                currentTypeObject.setImgResource(textValue);
                            } else if("heightImg".equalsIgnoreCase(tagName)){
                                currentTypeObject.setHeight(Integer.valueOf(textValue));
                            }
                        }
                        break;
                    default:
                }
                eventType = xpp.next();
            }
        }
        catch (Exception e){
            status = false;
            e.printStackTrace();
        }
        return  status;
    }
}
