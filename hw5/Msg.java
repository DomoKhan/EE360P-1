mport java. util .*;
Jublic class Msg {
int srcId, destId;
String tag ;
String msgBuf;
public Msg(int s, int t, String msgType, String buf) {
this. srcId = s;
destId = t;
tag = nisgType;
msgBuf = buf;
I
I
I
I
I
public int getSrcId () {
return srcId ;
public int getDestId () {
return destId ;
public String getTag() {
return tag;
public String getMessage () {
return msgBuf ;
public int getMessageInt () {
StringTokenizer st = new StringTokenizer (msgBuf);
return Integer. parseInt (st. nextToken 0);
I
public static Msg parsehlsg( StringTokenizer st ){
int srcId = Integer. parseInt (st. nextToken 0);
int destId = Integer. parseInt (st. nextToken 0);
String tag = st. nextToken ();
String buf = st. nextToken(”#);
return new Msg( srcId , destId , tag, buf);
1
public String tostring (){
String s = String. valueof (srcId)+” ” +
String . valueof ( destId )+ ” ” +
tag + ” ” + msgBuf + ”#”;
return s;
I
t 