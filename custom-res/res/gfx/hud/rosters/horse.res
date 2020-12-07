Haven Resource 1 src :  Horse.java /* Preprocessed source code */
/* $use: ui/croster */

import haven.*;
import haven.res.ui.croster.*;
import java.util.*;

public class Horse extends Entry {
    public int meat, milk;
    public int meatq, milkq, hideq;
    public int seedq;
    public int end, stam, mb;

    public Horse(long id, String name) {
	super(SIZE, id, name);
    }

    public void draw(GOut g) {
	drawbg(g);
	drawcol(g, HorseRoster.cols.get(0), 0, this, namerend, 0);
	drawcol(g, HorseRoster.cols.get(1), 1, q, quality, 1);
	drawcol(g, HorseRoster.cols.get(2), 1, end, null, 2);
	drawcol(g, HorseRoster.cols.get(3), 1, stam, null, 3);
	drawcol(g, HorseRoster.cols.get(4), 1, mb, null, 4);
	drawcol(g, HorseRoster.cols.get(5), 1, meat, null, 5);
	drawcol(g, HorseRoster.cols.get(6), 1, milk, null, 6);
	drawcol(g, HorseRoster.cols.get(7), 1, meatq, percent, 7);
	drawcol(g, HorseRoster.cols.get(8), 1, milkq, percent, 8);
	drawcol(g, HorseRoster.cols.get(9), 1, hideq, percent, 9);
	drawcol(g, HorseRoster.cols.get(10), 1, seedq, null, 10);
	super.draw(g);
    }
}

/* >wdg: HorseRoster */
src �
  HorseRoster.java /* Preprocessed source code */
/* $use: ui/croster */

import haven.*;
import haven.res.ui.croster.*;
import java.util.*;

public class HorseRoster extends CattleRoster<Horse> {
    public static List<Column> cols = initcols(
	new Column<Entry>("Name", Comparator.comparing((Entry e) -> e.name), 200),

	new Column<Horse>(Resource.classres(HorseRoster.class).pool.load("gfx/hud/rosters/quality", 2), Comparator.comparing((Horse e) -> e.q).reversed()),

	new Column<Horse>(Resource.classres(HorseRoster.class).pool.load("gfx/hud/rosters/endurance", 1), Comparator.comparing((Horse e) -> e.end).reversed()),
	new Column<Horse>(Resource.classres(HorseRoster.class).pool.load("gfx/hud/rosters/stamina", 1), Comparator.comparing((Horse e) -> e.stam).reversed()),
	new Column<Horse>(Resource.classres(HorseRoster.class).pool.load("gfx/hud/rosters/metabolism", 1), Comparator.comparing((Horse e) -> e.mb).reversed()),

	new Column<Horse>(Resource.classres(HorseRoster.class).pool.load("gfx/hud/rosters/meatquantity", 1), Comparator.comparing((Horse e) -> e.meat).reversed()),
	new Column<Horse>(Resource.classres(HorseRoster.class).pool.load("gfx/hud/rosters/milkquantity", 1), Comparator.comparing((Horse e) -> e.milk).reversed()),

	new Column<Horse>(Resource.classres(HorseRoster.class).pool.load("gfx/hud/rosters/meatquality", 1), Comparator.comparing((Horse e) -> e.meatq).reversed()),
	new Column<Horse>(Resource.classres(HorseRoster.class).pool.load("gfx/hud/rosters/milkquality", 1), Comparator.comparing((Horse e) -> e.milkq).reversed()),
	new Column<Horse>(Resource.classres(HorseRoster.class).pool.load("gfx/hud/rosters/hidequality", 1), Comparator.comparing((Horse e) -> e.hideq).reversed()),

	new Column<Horse>(Resource.classres(HorseRoster.class).pool.load("gfx/hud/rosters/breedingquality", 1), Comparator.comparing((Horse e) -> e.seedq).reversed())
    );
    protected List<Column> cols() {return(cols);}

    public static CattleRoster mkwidget(UI ui, Object... args) {
	return(new HorseRoster());
    }

    public Horse parse(Object... args) {
	int n = 0;
	long id = (Long)args[n++];
	String name = (String)args[n++];
	Horse ret = new Horse(id, name);
	ret.grp = (Integer)args[n++];
	ret.q = ((Number)args[n++]).doubleValue();
	ret.meat = (Integer)args[n++];
	ret.milk = (Integer)args[n++];
	ret.meatq = (Integer)args[n++];
	ret.milkq = (Integer)args[n++];
	ret.hideq = (Integer)args[n++];
	ret.seedq = (Integer)args[n++];
	ret.end = (Integer)args[n++];
	ret.stam = (Integer)args[n++];
	ret.mb = (Integer)args[n++];
	return(ret);
    }

    public TypeButton button() {
	return(typebtn(Resource.classres(HorseRoster.class).pool.load("gfx/hud/rosters/btn-horse", 2),
		       Resource.classres(HorseRoster.class).pool.load("gfx/hud/rosters/btn-horse-d", 2)));
    }
}
code �  Horse ����   4 a	  ,
  -
  .	 / 0 1 2 3	  4
  5	  6
 7 8	  9	  :
 ; <	  =	  >	  ?	  @	  A	  B	  C	  D	  E
  F G H meat I milk meatq milkq hideq seedq end stam mb <init> (JLjava/lang/String;)V Code LineNumberTable draw (Lhaven/GOut;)V 
SourceFile 
Horse.java I J $ K L ) M N O P Q R haven/res/ui/croster/Column S T U V W X Y Z [ \ T !  ] Z ^ "  #        _ T        ( ) Horse haven/res/ui/croster/Entry SIZE Lhaven/Coord; #(Lhaven/Coord;JLjava/lang/String;)V drawbg HorseRoster cols Ljava/util/List; java/util/List get (I)Ljava/lang/Object; namerend Ljava/util/function/Function; drawcol ](Lhaven/GOut;Lhaven/res/ui/croster/Column;DLjava/lang/Object;Ljava/util/function/Function;I)V q D java/lang/Double valueOf (D)Ljava/lang/Double; quality java/lang/Integer (I)Ljava/lang/Integer; percent horse.cjava !     	                                 !     "     #      $ %  &   &     
*� -� �    '   
     	   ( )  &  �    B*+� *+� �  � *� � *+� �  � *� 	� 
� � *+� �  � *� � � *+� �  � *� � � *+� �  � *� � � *+� �  � *� � � *+� �  � *� � � *+� �  � *� � � � *+� �  � *� � � � *+� 	�  � *� � � 	� *+� 
�  � *� � 
� *+� �    '   :         9  T  o  �  �  �  �    < A   *    `code �  HorseRoster ����   4
 ? c	  d e
  c f
  g h i
  j k
 
 l	  m n
  o	  p	  q	  r	  s	  t	  u	  v	  w	  x	  y
 z {	 z | }
 ~  �
  �
  �
 
 �
 � �	 � � � �   � � �
 # � �  � � �
 # � �  � �  � �  � �  � �  � �  � �  � � 	 � � 
 �
  � � cols Ljava/util/List; 	Signature /Ljava/util/List<Lhaven/res/ui/croster/Column;>; <init> ()V Code LineNumberTable ()Ljava/util/List; 1()Ljava/util/List<Lhaven/res/ui/croster/Column;>; mkwidget B(Lhaven/UI;[Ljava/lang/Object;)Lhaven/res/ui/croster/CattleRoster; parse ([Ljava/lang/Object;)LHorse; button #()Lhaven/res/ui/croster/TypeButton; 1([Ljava/lang/Object;)Lhaven/res/ui/croster/Entry; lambda$static$10 (LHorse;)Ljava/lang/Integer; lambda$static$9 lambda$static$8 lambda$static$7 lambda$static$6 lambda$static$5 lambda$static$4 lambda$static$3 lambda$static$2 lambda$static$1 (LHorse;)Ljava/lang/Double; lambda$static$0 0(Lhaven/res/ui/croster/Entry;)Ljava/lang/String; <clinit> ,Lhaven/res/ui/croster/CattleRoster<LHorse;>; 
SourceFile HorseRoster.java D E @ A HorseRoster java/lang/Long � � java/lang/String Horse D � java/lang/Integer � � � � java/lang/Number � � � � � � � � � � � � � � � � � � � � � � � � � � � gfx/hud/rosters/btn-horse � � � gfx/hud/rosters/btn-horse-d � � L M � � � � � � � � haven/res/ui/croster/Column Name BootstrapMethods � � � ^ � � � � � D � gfx/hud/rosters/quality � \ � � D � gfx/hud/rosters/endurance � R gfx/hud/rosters/stamina � gfx/hud/rosters/metabolism � gfx/hud/rosters/meatquantity � gfx/hud/rosters/milkquantity � gfx/hud/rosters/meatquality � gfx/hud/rosters/milkquality � gfx/hud/rosters/hidequality � gfx/hud/rosters/breedingquality � � � !haven/res/ui/croster/CattleRoster 	longValue ()J (JLjava/lang/String;)V intValue ()I grp I doubleValue ()D q D meat milk meatq milkq hideq seedq end stam mb haven/Resource classres #(Ljava/lang/Class;)Lhaven/Resource; pool Pool InnerClasses Lhaven/Resource$Pool; haven/Resource$Pool load � Named +(Ljava/lang/String;I)Lhaven/Resource$Named; typebtn =(Lhaven/Indir;Lhaven/Indir;)Lhaven/res/ui/croster/TypeButton; valueOf (I)Ljava/lang/Integer; java/lang/Double (D)Ljava/lang/Double; haven/res/ui/croster/Entry name Ljava/lang/String;
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
  �
  � initcols 0([Lhaven/res/ui/croster/Column;)Ljava/util/List; haven/Resource$Named � �  ] ^ [ \ Z R Y R X R W R V R U R T R S R Q R "java/lang/invoke/LambdaMetafactory metafactory Lookup �(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite; %java/lang/invoke/MethodHandles$Lookup java/lang/invoke/MethodHandles horse.cjava !  ?    	 @ A  B    C   D E  F        *� �    G       #  @ H  F        � �    G       6 B    I � J K  F         � Y� �    G       9 � L M  F  8     �=+�2� � B+�2� :� Y!� 	:+�2� 
� � +�2� � � +�2� 
� � +�2� 
� � +�2� 
� � +�2� 
� � +�2� 
� � +�2� 
� � +�2� 
� � +�2� 
� � +�2� 
� � �    G   B    =  >  ?  @ & A 7 B H C Y D j E { F � G � H � I � J � K � L  N O  F   @      � � � � � � � �    G       P  Q  PA L P  F        *+� �    G       #
 Q R  F         *� �  �    G       4
 S R  F         *� �  �    G       2
 T R  F         *� �  �    G       1
 U R  F         *� �  �    G       0
 V R  F         *� �  �    G       .
 W R  F         *� �  �    G       -
 X R  F         *� �  �    G       +
 Y R  F         *� �  �    G       *
 Z R  F         *� �  �    G       )
 [ \  F         *� � !�    G       '
 ] ^  F        *� "�    G       %  _ E  F  �     �� #Y� #Y$� %  � & ȷ 'SY� #Y� � (� � )  � &� * � +SY� #Y� � ,� � -  � &� * � +SY� #Y� � .� � /  � &� * � +SY� #Y� � 0� � 1  � &� * � +SY� #Y� � 2� � 3  � &� * � +SY� #Y� � 4� � 5  � &� * � +SY� #Y� � 6� � 7  � &� * � +SY� #Y� � 8� � 9  � &� * � +SY	� #Y� � :� � ;  � &� * � +SY
� #Y� � <� � =  � &� * � +S� >� �    G   6    $  % $ ' I ) n * � + � - � . 0* 1P 2v 4� $  �   p  �  � � � �  � � � �  � � � �  � � � �  � � � �  � � � �  � � � �  � � � �  � � � �  � � � �  � � � a    B    ` �     ~ z � 	 � z �	 � � codeentry "   wdg HorseRoster   ui/croster *  