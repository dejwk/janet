all: LongWrapper.java IntWrapper.java ShortWrapper.java BooleanWrapper.java \
     CharWrapper.java ByteWrapper.java FloatWrapper.java DoubleWrapper.java \
     LongRegion.java IntRegion.java ShortRegion.java BooleanRegion.java \
     CharRegion.java ByteRegion.java FloatRegion.java DoubleRegion.java

BooleanWrapper.java: _Type_Wrapper.template
	m4 -D_Type_=Boolean -D_JavaWrapper_=Boolean -D_type_=boolean -D_zeroWord_=false -D_zero_=false _Type_Wrapper.template > BooleanWrapper.java

ByteWrapper.java: _Type_Wrapper.template
	m4 -D_Type_=Byte -D_JavaWrapper_=Byte -D_type_=byte -D_zeroWord_=zero -D_zero_=(byte)0 _Type_Wrapper.template > ByteWrapper.java

ShortWrapper.java: _Type_Wrapper.template
	m4 -D_Type_=Short -D_JavaWrapper_=Short -D_type_=short -D_zeroWord_=zero -D_zero_=(short)0 _Type_Wrapper.template > ShortWrapper.java

CharWrapper.java: _Type_Wrapper.template
	m4 -D_Type_=Char -D_JavaWrapper_=Character -D_type_=char -D_zeroWord_=zero -D_zero_=(char)0 _Type_Wrapper.template > CharWrapper.java

IntWrapper.java: _Type_Wrapper.template
	m4 -D_Type_=Int -D_JavaWrapper_=Int -D_type_=int -D_zeroWord_=zero -D_zero_=0 _Type_Wrapper.template > IntWrapper.java

LongWrapper.java: _Type_Wrapper.template
	m4 -D_Type_=Long -D_JavaWrapper_=Long -D_type_=long -D_zeroWord_=zero -D_zero_=0L _Type_Wrapper.template > LongWrapper.java

FloatWrapper.java: _Type_Wrapper.template
	m4 -D_Type_=Float -D_JavaWrapper_=Float -D_type_=float -D_zeroWord_=zero -D_zero_=0.0f _Type_Wrapper.template > FloatWrapper.java

DoubleWrapper.java: _Type_Wrapper.template
	m4 -D_Type_=Double -D_JavaWrapper_=Double -D_type_=double -D_zeroWord_=zero -D_zero_=0.0 _Type_Wrapper.template > DoubleWrapper.java


BooleanRegion.java: _Type_Region.template
	m4 -D_Type_=Boolean -D_JavaWrapper_=Boolean -D_type_=boolean _Type_Region.template > BooleanRegion.java

ByteRegion.java: _Type_Region.template
	m4 -D_Type_=Byte -D_JavaWrapper_=Byte -D_type_=byte _Type_Region.template > ByteRegion.java

ShortRegion.java: _Type_Region.template
	m4 -D_Type_=Short -D_JavaWrapper_=Short -D_type_=short _Type_Region.template > ShortRegion.java

CharRegion.java: _Type_Region.template
	m4 -D_Type_=Char -D_JavaWrapper_=Character -D_type_=char _Type_Region.template > CharRegion.java

IntRegion.java: _Type_Region.template
	m4 -D_Type_=Int -D_JavaWrapper_=Int -D_type_=int _Type_Region.template > IntRegion.java

LongRegion.java: _Type_Region.template
	m4 -D_Type_=Long -D_JavaWrapper_=Long -D_type_=long _Type_Region.template > LongRegion.java

FloatRegion.java: _Type_Region.template
	m4 -D_Type_=Float -D_JavaWrapper_=Float -D_type_=float _Type_Region.template > FloatRegion.java

DoubleRegion.java: _Type_Region.template
	m4 -D_Type_=Double -D_JavaWrapper_=Double -D_type_=double _Type_Region.template > DoubleRegion.java

