import 'package:flutter/material.dart';

void main() {
  runApp(MaterialApp(
    
    home:Scaffold(
   appBar:AppBar(
    title:Text('Study to Container'),

   ),
    body:CustomContainer(),
    ),
  ),
  );
}

class CustomContainer extends StatelessWidget{
  const CustomContainer({super.key});

  @override
  Widget build(BuildContext context){
    return Container(
      width: 300,
      height: 300,
      
      padding: EdgeInsets.fromLTRB(10, 12, 10, 12),
      decoration: BoxDecoration(
color:Colors.red.shade200,
border:Border.all(color:Colors.red,width:5,style:BorderStyle.solid),
borderRadius: BorderRadius.circular(100),
boxShadow: [
  BoxShadow(color:Colors.black,offset:Offset(6, 6))
      ],
      ),
      child:Center(
        child:Container(color:Colors.yellow,
      child:Text('Hello Container'),
    ),
    ),
    );
  }
}
//ctrl+alt+L -> 정리 