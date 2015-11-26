public class Parser {

	/* 		OO PARSER AND BYTE-CODE GENERATOR FOR TINY PL
	 
	Grammar for TinyPL (using EBNF notation) is as follows:

	 program ->  decls stmts end
	 decls   ->  int idlist ;
	 idlist  ->  id { , id } 
	 stmts   ->  stmt [ stmts ]a
	 cmpdstmt->  '{' stmts '}'
	 stmt    ->  assign | cond | loop
	 assign  ->  id = expr ;
	 cond    ->  if '(' rexp ')' cmpdstmt [ else cmpdstmt ]
	 loop    ->  while '(' rexp ')' cmpdstmt  
	 rexp    ->  expr (< | > | =) expr
	 expr    ->  term   [ (+ | -) expr ]
	 term    ->  factor [ (* | /) term ]
	 factor  ->  int_lit | id | '(' expr ')'
	 
	Lexical:   id is a single character; 
		      int_lit is an unsigned integer;
			 equality operator is =, not ==

	Sample Program: Factorial
	 
	int n, i, f;
	n = 4;
	i = 1;
	f = 1;
	while (i < n) {
	  i = i + 1;
	  f= f * i;
	}
	end

	   Sample Program:  GCD
	   
	int x, y;
	x = 121;
	y = 132;
	while (x != y) {
	  if (x > y) 
	       { x = x - y; }
	  else { y = y - x; }
	}
	end

	 */

		public static void main(String[] args)  {
			System.out.println("Enter program and terminate with 'end'!\n");
			Lexer.lex();
			Program p = new Program();
			p.program();
		Code.output();
		}
	}

	class Program {
		Decls d;
		Stmts s;
		
		public void program(){
			d=new Decls();
			d.decls();
			s=new Stmts();
			
			s.stmts();
			if(Lexer.nextToken==Token.KEY_END)
			Code.store("return");
			// {Lexer.lex();s=new Stmts();
			//s.stmts();}
			//if(Lexer.nextToken==Token.KEY_END){
				//System.out.println("end");
				//Lexer.lex();
				 
			//}
		}
		 
	}

	class Decls {
		 Idlist i;
		 Decls d;
		 public void decls(){
			// if(Lexer.nextToken==Token.KEY_INT){
			//System.out.println("int ");
				 Lexer.lex();
			i=new Idlist();
			i.idlist();
			if (Lexer.nextToken==Token.SEMICOLON){
			//	System.out.println(";");
				Lexer.lex();
				if (Lexer.nextToken==Token.KEY_INT){
				d=new Decls();
				d.decls();
				}
			}
//			 }
			 
		 }
	}

	class Idlist {
		int i;
		char id;
		 Idlist idlst;
		 public void idlist(){
			// if(Lexer.nextToken==Token.ID){
				 id=Lexer.ident;
				 i=Code.record(id);  
				//System.out.println(id);
				 Lexer.lex();
				 if(Lexer.nextToken==Token.COMMA){
					//System.out.print(", ");
					Lexer.lex();
					idlst=new Idlist();
					idlst.idlist();
				 }
			 }
		// }
	}

	class Stmt {
		Assign a;
		Cond cd;
		Loop lp;
		public void stmt(){
			//System.out.println(Lexer.nextToken);
			switch(Lexer.nextToken){
			case Token.ID:
				//System.out.println(Lexer.ch);
				a=new Assign();
				a.assign();
				break;
			case Token.KEY_WHILE:
				lp=new Loop();
				lp.loop();
				break;
			case Token.KEY_IF:
				cd=new Cond();
				cd.cond();
				break;
			default:
				break;	
			}
				
		}
		 
	} 

	class Stmts {
		Stmt st;
		Stmts sts;
		public void stmts(){
			st=new Stmt();
			st.stmt();
			//System.out.println(Lexer.nextChar);
			//while(Lexer.nextToken!=Token.KEY_END){//if(Lexer.nextToken==Token.RIGHT_BRACE){}
			if (Lexer.nextToken!=Token.KEY_END&&Lexer.nextToken!=Token.RIGHT_BRACE){
			//Lexer.lex();
				sts=new Stmts();
				//System.out.println("..........");
			sts.stmts();
			}
			}//System.out.println(Lexer.nextToken);
		
		 
	}

	class Assign {
		int i;
                char id;
		char op;
		Expr e;
		public void assign(){
			//if(Lexer.nextToken==Token.ID){
			id=Lexer.ident;
			//System.out.println(id);
			
			i=Code.record(id);                    // store n,i,f
			Lexer.lex();
			//Lexer.nextToken==Token.ASSIGN_OP             //?
				op=Lexer.nextChar;
				//System.out.println(op);
				
				Lexer.lex();
				e = new Expr();
				e.expr();
				Lexer.lex();
				//Lexer.lex();
				//System.out.println(Lexer.nextChar);
				Code.storeindx(i);
				Code.store("istore_");
			
			//}
			
		}
		 
	}

	class Cond {
		 Rexpr r;
		 Cmpdstmt cm1;
		 Cmpdstmt cm2;
		 int gotoposition;
		 int position;
		 int position_for_cmp;
		 public void cond(){
			 //if(Lexer.nextToken==Token.KEY_IF){
				
				 Lexer.lex();    // now nextToken=="(",skip it
				
				 Lexer.lex();    //  Rexpr
				 r=new Rexpr();
				 r.rexpr();
				 position_for_cmp=Code.returnpostion()-3;
					  // store 8 into mark[0], 8 means index 8
				 
				 Lexer.lex();
				 cm1=new Cmpdstmt();
				 cm1.cmpdstmt();
				 
				 if(Lexer.nextToken==Token.KEY_ELSE){
				     gotoposition=Code.returnpostion();
					 Code.store("goto ");
				     Code.store(" ");
						Code.store(" ");
						position=Code.returnpostion();
					     Code.storemk(position_for_cmp);
					     Code.storemk(position);
					 Lexer.lex();      // refer to cmpdstmt;
				cm2=new Cmpdstmt();
				cm2.cmpdstmt();
				Code.storemk(gotoposition);
				gotoposition=Code.returnpostion();
				Code.storemk(gotoposition);
				}
				 else{ Code.storemk(position_for_cmp);
				position_for_cmp=Code.returnpostion();
				Code.storemk(position_for_cmp);}
				 
			 //}
		 }
	}

	class Loop {
		char op1;
		char op2;
		Rexpr r;
		int positon;
		int position_for_cmp;
		Cmpdstmt cm;
		public void loop(){
			positon=Code.returnpostion();
			//System.out.print("while");
			Lexer.lex();  // nextToken now refer to "("
			op1=Lexer.nextChar;  //store"(" in op1
			//System.out.println(op1);
			
			Lexer.lex();  // refer to rexpr
			r=new Rexpr();
			r.rexpr();
			position_for_cmp=Code.returnpostion()-3;
			//System.out.println();
			op2=Lexer.nextChar; // op2 stores ")"
			Lexer.lex();        // refer to cmpdstmt
			cm=new Cmpdstmt();
			cm.cmpdstmt();
			Code.storeindx(positon);
			Code.store("goto ");
			Code.store(" ");
			Code.store(" ");
			//System.out.println(Lexer.nextToken);
			Code.storemk(position_for_cmp);
			position_for_cmp=Code.returnpostion();
			Code.storemk(position_for_cmp);
		}
		 
	}

	class Cmpdstmt {
		Stmts sts;
		char op1;
		char op2;
		public void cmpdstmt(){
			//if (Lexer.nextToken==Token.LEFT_BRACE){         //?
				op1=Lexer.nextChar; // store "{"
				//System.out.println(op1);
				Lexer.lex();
				sts=new Stmts();
				sts.stmts();
				op2=Lexer.nextChar;// store "}"
				//System.out.println(op2);
				Lexer.lex();
				//System.out.println(op2);
				//System.out.println(Lexer.nextChar);
			}
		//}
		 
	}

	class Rexpr {
		 Expr e1;
		 Expr e2;
		 char op;
		 public void rexpr(){
			 e1=new Expr();
			 e1.expr();
			 //if (Lexer.nextToken==Token.LESSER_OP||Lexer.nextToken==Token.GREATER_OP||Lexer.nextToken==Token.ASSIGN_OP||Lexer.nextToken==Token.NOT_EQ){
				 op=Lexer.nextChar;
				 
				// System.out.println(op);
				 Lexer.lex();
				 e2=new Expr();
				 e2.expr();
				 Code.storeindx(-1);
				 Code.store(Code.opcode(op));
				 Code.store(" ");
				 Code.store(" ");
			 //}
		 }
	}

	class Expr {  
		Term t;
		Expr e;
		char op;
		
		public void expr(){
			t=new Term();
			t.term();
			if (Lexer.nextToken==Token.ADD_OP||Lexer.nextToken==Token.SUB_OP){
				op= Lexer.nextChar;
				//System.out.println(op);
				Lexer.lex();
				e= new Expr();
				e.expr();
				Code.store(Code.opcode(op));
				//Code.gen(Code.opcode(op));
			}
		}
		 
	}

	class Term {  
		Factor f;
		Term tm;
		char op;
		public void term(){
			f=new Factor();
			f.factor();
			if (Lexer.nextToken==Token.MULT_OP||Lexer.nextToken==Token.DIV_OP){
				op=Lexer.nextChar;
				Lexer.lex();
				tm=new Term();
				tm.term();
				
				Code.store(Code.opcode(op));
				//Code.gen(Code.opcode(op));
			}
		}
	}

	class Factor {  
		 int int_lit;
		 char id;
		 char op1;
		 char op2;
		 Expr e;
		 public void factor(){
			 switch(Lexer.nextToken){
			 case Token.INT_LIT:
				 int_lit=Lexer.intValue;
				// System.out.println(int_lit);
				 
				 Lexer.lex();
				// System.out.println(Lexer.nextChar);
				 Code.storeindx(int_lit);
				 if(int_lit<6)
				 Code.store("iconst_");
				 else if(int_lit<128)
					 {Code.store("bipush ");
				     Code.store(" ");}
				 else {Code.store("sipush ");
				 Code.store(" ");
				 Code.store(" ");
				 }//Code.gen("iconst_" + int_lit);
				 break;
			 case Token.ID:
				 int i;
				 id=Lexer.ident;
				// System.out.println(id);
				 i=Code.record(id);
				 Code.storeindx(i);
				 if(i<4)
				 Code.store("iload_");
				 else Code.store("iload ");
				 //Code.gen("iload_"+Code.load);
				 Lexer.lex();
				 break;
			 case Token.LEFT_PAREN:
				 op1=Lexer.nextChar; //store "("
				 Lexer.lex();
				 e=new Expr();
				 e.expr();   // nextToken==Right_PAREN.
				 op2=Lexer.nextChar; //store ")"
				 Lexer.lex();//skip ")"
				 break;
			default:
				 break;
			 }
		 }
	}

	class Code {
		static String[] code = new String[100];
		static String[] codest = new String[100];
		static int codeptr = 0;
		static char[] iden=new char[100];
		static int idenstore=0;
		static int load=0;
		static int[] mark=new int [100];          // for if_cmp
		static int k=0;
		static int[] index=new int[100];          // indx
		static int j=0;
		
		public static int record (char id){
			if(idenstore==0){iden[idenstore]=id;idenstore++;load=0;}
			int markrcd=0;
			for(int i=0; i<idenstore;i++){
			if(iden[i]==id){load=i;markrcd=1;}
			}
			if(markrcd==0){iden[idenstore]=id;load=idenstore;idenstore++;}
		     return load;
		}
               
		public static void storemk(int i){
		 mark[j]=i; j++;
		}
		
		public static int returnpostion(){
		
		 return codeptr;
		}
		
		//public static int record(char id){
			//idenstore++;
			//iden[idenstore]=id;
              //          return idenstore;
			
	//	}
		
		public static void store(String s){
		        codest[codeptr]=s;	
		        codeptr++;
		}
		public static void storeindx(int i){
			index[codeptr]=i;
		}
		public static String opcode(char op) {
			switch(op) {
			case '+' : return "iadd";
			case '-':  return "isub";
			case '*':  return "imul";
			case '/':  return "idiv";
			case '<':  return "if_icmpge ";
			case '>':  return "if_icmple ";
			case '=':  return "if_icmpne ";
			case '!':  return "if_icmpeq ";
			default:   return "";
			}
		}
		
		public static void output() {
			for(k=0;k<j;k=k+2){if(index[mark[k]]!=-1)index[mark[k]]=mark[k+1];}
			for (int i=0; i<codeptr; i++){
				 if(codest[i]!="goto "&&index[i]!=-1&&codest[i]!="imul"&&codest[i]!="idiv"&&codest[i]!="iadd"&&codest[i]!="isub"&&codest[i]!="return"&&codest[i]!="bipush "&&codest[i]!="sipush ") 
					 System.out.println(i + ": " + codest[i]+index[i]);
				if(codest[i]=="goto "){
				System.out.println(i + ": " + codest[i]+index[i]);
			    i=i+2;
				}
				if(codest[i]=="sipush "){
					System.out.println(i + ": " + codest[i]+index[i]);
				    i=i+2;
					}
				 if (index[i]==-1){k=0;
				   while(i!=mark[k])k=k+2;
				    System.out.println(i+": "+codest[i]+mark[k+1]);
				     i=i+2;
			              }
			    if(codest[i]=="imul"||codest[i]=="idiv"||codest[i]=="iadd"||codest[i]=="isub"||codest[i]=="return")
			    System.out.println(i+": "+codest[i]);	
			    if(codest[i]=="bipush "){System.out.println(i + ": " + codest[i]+index[i]);i=i+1;}
			   
			}
			}
	}


	

