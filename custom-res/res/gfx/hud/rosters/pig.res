Haven Resource 1 src �  Pig.java /* Preprocessed source code */
/* $use: ui/croster */

import haven.*;
import haven.res.ui.croster.*;
import java.util.*;

public class Pig extends Entry {
    public int meat, milk;
    public int meatq, milkq, hideq;
    public int seedq;
    public int prc;

    public Pig(long id, String name) {
	super(SIZE, id, name);
    }

    public void draw(GOut g) {
	drawbg(g);
	drawcol(g, PigRoster.cols.get(0), 0, this, namerend, 0);
	drawcol(g, PigRoster.cols.get(1), 1, q, quality, 1);
	drawcol(g, PigRoster.cols.get(2), 1, prc, null, 2);
	drawcol(g, PigRoster.cols.get(3), 1, meat, null, 3);
	drawcol(g, PigRoster.cols.get(4), 1, milk, null, 4);
	drawcol(g, PigRoster.cols.get(5), 1, meatq, percent, 5);
	drawcol(g, PigRoster.cols.get(6), 1, milkq, percent, 6);
	drawcol(g, PigRoster.cols.get(7), 1, hideq, percent, 7);
	drawcol(g, PigRoster.cols.get(8), 1, seedq, null, 7);
	super.draw(g);
    }
}

/* >wdg: PigRoster */
src =	  PigRoster.java /* Preprocessed source code */
/* $use: ui/croster */

import haven.*;
import haven.res.ui.croster.*;
import java.util.*;

public class PigRoster extends CattleRoster<Pig> {
    public static List<Column> cols = initcols(
	new Column<Entry>("Name", Comparator.comparing((Entry e) -> e.name), 200),

	new Column<Pig>(Resource.classres(PigRoster.class).pool.load("gfx/hud/rosters/quality", 2), Comparator.comparing((Pig e) -> e.q).reversed()),

	new Column<Pig>(Resource.classres(PigRoster.class).pool.load("gfx/hud/rosters/trufflesnout", 1), Comparator.comparing((Pig e) -> e.prc).reversed()),

	new Column<Pig>(Resource.classres(PigRoster.class).pool.load("gfx/hud/rosters/meatquantity", 1), Comparator.comparing((Pig e) -> e.meat).reversed()),
	new Column<Pig>(Resource.classres(PigRoster.class).pool.load("gfx/hud/rosters/milkquantity", 1), Comparator.comparing((Pig e) -> e.milk).reversed()),

	new Column<Pig>(Resource.classres(PigRoster.class).pool.load("gfx/hud/rosters/meatquality", 1), Comparator.comparing((Pig e) -> e.meatq).reversed()),
	new Column<Pig>(Resource.classres(PigRoster.class).pool.load("gfx/hud/rosters/milkquality", 1), Comparator.comparing((Pig e) -> e.milkq).reversed()),
	new Column<Pig>(Resource.classres(PigRoster.class).pool.load("gfx/hud/rosters/hidequality", 1), Comparator.comparing((Pig e) -> e.hideq).reversed()),

	new Column<Pig>(Resource.classres(PigRoster.class).pool.load("gfx/hud/rosters/breedingquality", 1), Comparator.comparing((Pig e) -> e.seedq).reversed())
    );
    protected List<Column> cols() {return(cols);}

    public static CattleRoster mkwidget(UI ui, Object... args) {
	return(new PigRoster());
    }

    public Pig parse(Object... args) {
	int n = 0;
	long id = (Long)args[n++];
	String name = (String)args[n++];
	Pig ret = new Pig(id, name);
	ret.grp = (Integer)args[n++];
	ret.q = ((Number)args[n++]).doubleValue();
	ret.meat = (Integer)args[n++];
	ret.milk = (Integer)args[n++];
	ret.meatq = (Integer)args[n++];
	ret.milkq = (Integer)args[n++];
	ret.hideq = (Integer)args[n++];
	ret.seedq = (Integer)args[n++];
	ret.prc = (Integer)args[n++];
	return(ret);
    }

    public TypeButton button() {
	return(typebtn(Resource.classres(PigRoster.class).pool.load("gfx/hud/rosters/btn-pig", 2),
		       Resource.classres(PigRoster.class).pool.load("gfx/hud/rosters/btn-pig-d", 2)));
    }
}
code f  Pig ����   4 [	  (
  )
  *	 + , - . /	  0
  1	  2
 3 4	  5	  6
 7 8	  9	  :	  ;	  <	  =	  >	  ?
  @ A B meat I milk meatq milkq hideq seedq prc <init> (JLjava/lang/String;)V Code LineNumberTable draw (Lhaven/GOut;)V 
SourceFile Pig.java C D   E F % G H I J K L haven/res/ui/croster/Column M N O P Q R S T U V N   W T X       Y N       $ % Pig haven/res/ui/croster/Entry SIZE Lhaven/Coord; #(Lhaven/Coord;JLjava/lang/String;)V drawbg 	PigRoster cols Ljava/util/List; java/util/List get (I)Ljava/lang/Object; namerend Ljava/util/function/Function; drawcol ](Lhaven/GOut;Lhaven/res/ui/croster/Column;DLjava/lang/Object;Ljava/util/function/Function;I)V q D java/lang/Double valueOf (D)Ljava/lang/Double; quality java/lang/Integer (I)Ljava/lang/Integer; percent 	pig.cjava !                                             !  "   &     
*� -� �    #   
     	   $ %  "  L    *+� *+� �  � *� � *+� �  � *� 	� 
� � *+� �  � *� � � *+� �  � *� � � *+� �  � *� � � *+� �  � *� � � � *+� �  � *� � � � *+� �  � *� � � � *+� �  � *� � � *+� �    #   2         9  T  o  �  �  �  �     &    Zcode P  PigRoster ����   4 �
 9 [	  \ ]
  [ ^
  _ ` a
  b c
 
 d	  e f
  g	  h	  i	  j	  k	  l	  m	  n	  o
 p q	 p r s
 t u v
  w
  x
 
 y
 z {	 | } ~    � � �
 ! � �  � � �
 ! � �  � �  � �  � �  � �  � �  � �  �
  � � cols Ljava/util/List; 	Signature /Ljava/util/List<Lhaven/res/ui/croster/Column;>; <init> ()V Code LineNumberTable ()Ljava/util/List; 1()Ljava/util/List<Lhaven/res/ui/croster/Column;>; mkwidget B(Lhaven/UI;[Ljava/lang/Object;)Lhaven/res/ui/croster/CattleRoster; parse ([Ljava/lang/Object;)LPig; button #()Lhaven/res/ui/croster/TypeButton; 1([Ljava/lang/Object;)Lhaven/res/ui/croster/Entry; lambda$static$8 (LPig;)Ljava/lang/Integer; lambda$static$7 lambda$static$6 lambda$static$5 lambda$static$4 lambda$static$3 lambda$static$2 lambda$static$1 (LPig;)Ljava/lang/Double; lambda$static$0 0(Lhaven/res/ui/croster/Entry;)Ljava/lang/String; <clinit> *Lhaven/res/ui/croster/CattleRoster<LPig;>; 
SourceFile PigRoster.java > ? : ; 	PigRoster java/lang/Long � � java/lang/String Pig > � java/lang/Integer � � � � java/lang/Number � � � � � � � � � � � � � � � � � � � � � � � gfx/hud/rosters/btn-pig � � � gfx/hud/rosters/btn-pig-d � � F G � � � � � � � � haven/res/ui/croster/Column Name BootstrapMethods � � � V � � � � � > � gfx/hud/rosters/quality � T � � > � gfx/hud/rosters/trufflesnout � L gfx/hud/rosters/meatquantity � gfx/hud/rosters/milkquantity � gfx/hud/rosters/meatquality � gfx/hud/rosters/milkquality � gfx/hud/rosters/hidequality � gfx/hud/rosters/breedingquality � � � !haven/res/ui/croster/CattleRoster 	longValue ()J (JLjava/lang/String;)V intValue ()I grp I doubleValue ()D q D meat milk meatq milkq hideq seedq prc haven/Resource classres #(Ljava/lang/Class;)Lhaven/Resource; pool Pool InnerClasses Lhaven/Resource$Pool; haven/Resource$Pool load � Named +(Ljava/lang/String;I)Lhaven/Resource$Named; typebtn =(Lhaven/Indir;Lhaven/Indir;)Lhaven/res/ui/croster/TypeButton; valueOf (I)Ljava/lang/Integer; java/lang/Double (D)Ljava/lang/Double; haven/res/ui/croster/Entry name Ljava/lang/String;
 � � &(Ljava/lang/Object;)Ljava/lang/Object;
  � apply ()Ljava/util/function/Function; java/util/Comparator 	comparing 5(Ljava/util/function/Function;)Ljava/util/Comparator; ,(Ljava/lang/String;Ljava/util/Comparator;I)V
  � reversed ()Ljava/util/Comparator; &(Lhaven/Indir;Ljava/util/Comparator;)V
  �
  �
  �
  �
  �
  �
  � initcols 0([Lhaven/res/ui/croster/Column;)Ljava/util/List; haven/Resource$Named � � � U V S T R L Q L P L O L N L M L K L "java/lang/invoke/LambdaMetafactory metafactory � Lookup �(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite; � %java/lang/invoke/MethodHandles$Lookup java/lang/invoke/MethodHandles 	pig.cjava !  9    	 : ;  <    =   > ?  @        *� �    A       !  : B  @        � �    A       2 <    C � D E  @         � Y� �    A       5 � F G  @       �=+�2� � B+�2� :� Y!� 	:+�2� 
� � +�2� � � +�2� 
� � +�2� 
� � +�2� 
� � +�2� 
� � +�2� 
� � +�2� 
� � +�2� 
� � �    A   :    9  :  ;  < & = 7 > H ? Y @ j A { B � C � D � E � F  H I  @   @      � � � � � � � �    A       J  K  JA F J  @        *+� �    A       !
 K L  @         *� � �    A       0
 M L  @         *� � �    A       .
 N L  @         *� � �    A       -
 O L  @         *� � �    A       ,
 P L  @         *� � �    A       *
 Q L  @         *� � �    A       )
 R L  @         *� � �    A       '
 S T  @         *� � �    A       %
 U V  @        *�  �    A       #  W ?  @  �     N	� !Y� !Y"� #  � $ ȷ %SY� !Y� � &� � '  � $� ( � )SY� !Y� � *� � +  � $� ( � )SY� !Y� � ,� � -  � $� ( � )SY� !Y� � .� � /  � $� ( � )SY� !Y� � 0� � 1  � $� ( � )SY� !Y� � 2� � 3  � $� ( � )SY� !Y� � 4� � 5  � $� ( � )SY� !Y� � 6� � 7  � $� ( � )S� 8� �    A   .    "  # $ % I ' n ) � * � , � - .* 0G "  �   \ 	 �  � � � �  � � � �  � � � �  � � � �  � � � �  � � � �  � � � �  � � � �  � � � Y    � <    X �     t p � 	 � p �	 � � � codeentry     wdg PigRoster   ui/croster *  