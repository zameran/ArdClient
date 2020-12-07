Haven Resource 1 src �  Goat.java /* Preprocessed source code */
/* $use: ui/croster */

import haven.*;
import haven.res.ui.croster.*;
import java.util.*;

public class Goat extends Entry {
    public int meat, milk, wool;
    public int meatq, milkq, woolq, hideq;
    public int seedq;

    public Goat(long id, String name) {
	super(SIZE, id, name);
    }

    public void draw(GOut g) {
	drawbg(g);
	drawcol(g, GoatRoster.cols.get(0), 0, this, namerend, 0);
	drawcol(g, GoatRoster.cols.get(1), 1, q, quality, 1);
	drawcol(g, GoatRoster.cols.get(2), 1, meat, null, 2);
	drawcol(g, GoatRoster.cols.get(3), 1, milk, null, 3);
	drawcol(g, GoatRoster.cols.get(4), 1, wool, null, 4);
	drawcol(g, GoatRoster.cols.get(5), 1, meatq, percent, 5);
	drawcol(g, GoatRoster.cols.get(6), 1, milkq, percent, 6);
	drawcol(g, GoatRoster.cols.get(7), 1, woolq, percent, 7);
	drawcol(g, GoatRoster.cols.get(8), 1, hideq, percent, 8);
	drawcol(g, GoatRoster.cols.get(9), 1, seedq, null, 9);
	super.draw(g);
    }
}

/* >wdg: GoatRoster */
src 
  GoatRoster.java /* Preprocessed source code */
/* $use: ui/croster */

import haven.*;
import haven.res.ui.croster.*;
import java.util.*;

public class GoatRoster extends CattleRoster<Goat> {
    public static List<Column> cols = initcols(
	new Column<Entry>("Name", Comparator.comparing((Entry e) -> e.name), 200),

	new Column<Goat>(Resource.classres(GoatRoster.class).pool.load("gfx/hud/rosters/quality", 2), Comparator.comparing((Goat e) -> e.q).reversed()),

	new Column<Goat>(Resource.classres(GoatRoster.class).pool.load("gfx/hud/rosters/meatquantity", 1), Comparator.comparing((Goat e) -> e.meat).reversed()),
	new Column<Goat>(Resource.classres(GoatRoster.class).pool.load("gfx/hud/rosters/milkquantity", 1), Comparator.comparing((Goat e) -> e.milk).reversed()),
	new Column<Goat>(Resource.classres(GoatRoster.class).pool.load("gfx/hud/rosters/woolquantity", 1), Comparator.comparing((Goat e) -> e.milk).reversed()),

	new Column<Goat>(Resource.classres(GoatRoster.class).pool.load("gfx/hud/rosters/meatquality", 1), Comparator.comparing((Goat e) -> e.meatq).reversed()),
	new Column<Goat>(Resource.classres(GoatRoster.class).pool.load("gfx/hud/rosters/milkquality", 1), Comparator.comparing((Goat e) -> e.milkq).reversed()),
	new Column<Goat>(Resource.classres(GoatRoster.class).pool.load("gfx/hud/rosters/woolquality", 1), Comparator.comparing((Goat e) -> e.milkq).reversed()),
	new Column<Goat>(Resource.classres(GoatRoster.class).pool.load("gfx/hud/rosters/hidequality", 1), Comparator.comparing((Goat e) -> e.hideq).reversed()),

	new Column<Goat>(Resource.classres(GoatRoster.class).pool.load("gfx/hud/rosters/breedingquality", 1), Comparator.comparing((Goat e) -> e.seedq).reversed())
    );
    protected List<Column> cols() {return(cols);}

    public static CattleRoster mkwidget(UI ui, Object... args) {
	return(new GoatRoster());
    }

    public Goat parse(Object... args) {
	int n = 0;
	long id = (Long)args[n++];
	String name = (String)args[n++];
	Goat ret = new Goat(id, name);
	ret.grp = (Integer)args[n++];
	ret.q = ((Number)args[n++]).doubleValue();
	ret.meat = (Integer)args[n++];
	ret.milk = (Integer)args[n++];
	ret.wool = (Integer)args[n++];
	ret.meatq = (Integer)args[n++];
	ret.milkq = (Integer)args[n++];
	ret.woolq = (Integer)args[n++];
	ret.hideq = (Integer)args[n++];
	ret.seedq = (Integer)args[n++];
	return(ret);
    }

    public TypeButton button() {
	return(typebtn(Resource.classres(GoatRoster.class).pool.load("gfx/hud/rosters/btn-goat", 4),
		       Resource.classres(GoatRoster.class).pool.load("gfx/hud/rosters/btn-goat-d", 3)));
    }
}
code �  Goat ����   4 ^	  *
  +
  ,	 - . / 0 1	  2
  3	  4
 5 6	  7	  8
 9 :	  ;	  <	  =	  >	  ?	  @	  A	  B
  C D E meat I milk wool meatq milkq woolq hideq seedq <init> (JLjava/lang/String;)V Code LineNumberTable draw (Lhaven/GOut;)V 
SourceFile 	Goat.java F G " H I ' J K L M N O haven/res/ui/croster/Column P Q R S T U V W X Y Q   Z W [       \ Q        !  & ' Goat haven/res/ui/croster/Entry SIZE Lhaven/Coord; #(Lhaven/Coord;JLjava/lang/String;)V drawbg 
GoatRoster cols Ljava/util/List; java/util/List get (I)Ljava/lang/Object; namerend Ljava/util/function/Function; drawcol ](Lhaven/GOut;Lhaven/res/ui/croster/Column;DLjava/lang/Object;Ljava/util/function/Function;I)V q D java/lang/Double valueOf (D)Ljava/lang/Double; quality java/lang/Integer (I)Ljava/lang/Integer; percent 
goat.cjava !                                           !      " #  $   &     
*� -� �    %   
     	   & '  $  o    '*+� *+� �  � *� � *+� �  � *� 	� 
� � *+� �  � *� � � *+� �  � *� � � *+� �  � *� � � *+� �  � *� � � � *+� �  � *� � � � *+� �  � *� � � � *+� �  � *� � � � *+� 	�  � *� � 	� *+� �    %   6         9  T  o  �  �  �  �  ! &   (    ]code +  GoatRoster ����   4 �
 < _	  ` a
  _ b
  c d e
  f g
 
 h	  i j
  k	  l	  m	  n	  o	  p	  q	  r	  s	  t
 u v	 u w x
 y z {
  |
  }
 
 ~
  �	 � � � �   � � �
 " � �  � � �
 " � �  � �  � �  � �  � �  � �  � �  � � 	 �
  � � cols Ljava/util/List; 	Signature /Ljava/util/List<Lhaven/res/ui/croster/Column;>; <init> ()V Code LineNumberTable ()Ljava/util/List; 1()Ljava/util/List<Lhaven/res/ui/croster/Column;>; mkwidget B(Lhaven/UI;[Ljava/lang/Object;)Lhaven/res/ui/croster/CattleRoster; parse ([Ljava/lang/Object;)LGoat; button #()Lhaven/res/ui/croster/TypeButton; 1([Ljava/lang/Object;)Lhaven/res/ui/croster/Entry; lambda$static$9 (LGoat;)Ljava/lang/Integer; lambda$static$8 lambda$static$7 lambda$static$6 lambda$static$5 lambda$static$4 lambda$static$3 lambda$static$2 lambda$static$1 (LGoat;)Ljava/lang/Double; lambda$static$0 0(Lhaven/res/ui/croster/Entry;)Ljava/lang/String; <clinit> +Lhaven/res/ui/croster/CattleRoster<LGoat;>; 
SourceFile GoatRoster.java A B = > 
GoatRoster java/lang/Long � � java/lang/String Goat A � java/lang/Integer � � � � java/lang/Number � � � � � � � � � � � � � � � � � � � � � � � � � gfx/hud/rosters/btn-goat � � � gfx/hud/rosters/btn-goat-d � � I J � � � � � � � � haven/res/ui/croster/Column Name BootstrapMethods � � � Z � � � � � A � gfx/hud/rosters/quality � X � � A � gfx/hud/rosters/meatquantity � O gfx/hud/rosters/milkquantity � gfx/hud/rosters/woolquantity � gfx/hud/rosters/meatquality � gfx/hud/rosters/milkquality � gfx/hud/rosters/woolquality � gfx/hud/rosters/hidequality � gfx/hud/rosters/breedingquality � � � !haven/res/ui/croster/CattleRoster 	longValue ()J (JLjava/lang/String;)V intValue ()I grp I doubleValue ()D q D meat milk wool meatq milkq woolq hideq seedq haven/Resource classres #(Ljava/lang/Class;)Lhaven/Resource; pool Pool InnerClasses Lhaven/Resource$Pool; haven/Resource$Pool load � Named +(Ljava/lang/String;I)Lhaven/Resource$Named; typebtn =(Lhaven/Indir;Lhaven/Indir;)Lhaven/res/ui/croster/TypeButton; valueOf (I)Ljava/lang/Integer; java/lang/Double (D)Ljava/lang/Double; haven/res/ui/croster/Entry name Ljava/lang/String;
 � � &(Ljava/lang/Object;)Ljava/lang/Object;
  � apply ()Ljava/util/function/Function; java/util/Comparator 	comparing 5(Ljava/util/function/Function;)Ljava/util/Comparator; ,(Ljava/lang/String;Ljava/util/Comparator;I)V
  � reversed ()Ljava/util/Comparator; &(Lhaven/Indir;Ljava/util/Comparator;)V
  �
  �
  �
  �
  �
  �
  �
  � initcols 0([Lhaven/res/ui/croster/Column;)Ljava/util/List; haven/Resource$Named � � � Y Z W X V O U O T O S O R O Q O P O N O "java/lang/invoke/LambdaMetafactory metafactory � Lookup �(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite; � %java/lang/invoke/MethodHandles$Lookup java/lang/invoke/MethodHandles 
goat.cjava !  <    	 = >  ?    @   A B  C        *� �    D       !  = E  C        � �    D       2 ?    F � G H  C         � Y� �    D       5 � I J  C  #     �=+�2� � B+�2� :� Y!� 	:+�2� 
� � +�2� � � +�2� 
� � +�2� 
� � +�2� 
� � +�2� 
� � +�2� 
� � +�2� 
� � +�2� 
� � +�2� 
� � �    D   >    9  :  ;  < & = 7 > H ? Y @ j A { B � C � D � E � F � G  K L  C   @      � � � � � � � �    D       K  L  KA I M  C        *+� �    D       !
 N O  C         *� � �    D       0
 P O  C         *� � �    D       .
 Q O  C         *� � �    D       -
 R O  C         *� � �    D       ,
 S O  C         *� � �    D       +
 T O  C         *� � �    D       )
 U O  C         *� � �    D       (
 V O  C         *� � �    D       '
 W X  C         *� �  �    D       %
 Y Z  C        *� !�    D       #  [ B  C  �     t
� "Y� "Y#� $  � % ȷ &SY� "Y� � '� � (  � %� ) � *SY� "Y� � +� � ,  � %� ) � *SY� "Y� � -� � .  � %� ) � *SY� "Y� � /� � 0  � %� ) � *SY� "Y� � 1� � 2  � %� ) � *SY� "Y� � 3� � 4  � %� ) � *SY� "Y� � 5� � 6  � %� ) � *SY� "Y� � 7� � 8  � %� ) � *SY	� "Y� � 9� � :  � %� ) � *S� ;� �    D   2    "  # $ % I ' n ( � ) � + � , -* .P 0m "  �   f 
 �  � � � �  � � � �  � � � �  � � � �  � � � �  � � � �  � � � �  � � � �  � � � �  � � � ]    � ?    \ �     y u � 	 � u �	 � � � codeentry !   wdg GoatRoster   ui/croster *  