package ihm

import java.awt.Color
import scala.swing._
import scala.swing.BorderPanel.Position._
import scala.swing.event._
import audio.AudioFile
import bd._
import java.io._
import javax.swing.JPopupMenu
import javax.swing.Renderer

object  Fenetre extends SimpleSwingApplication{
  
  var selectedAudio=""
  var gestPlayer:AudioFile=null
  val gestAudio = GestionDataBase   
  val listView = new ListView(GestionDataBase.getAll)
  listView.background_=(Color.ORANGE)
  var button =new Button{ text =" Let's Play ! Choise your song!"
      foreground = Color.red
      background = Color.PINK
  }

  // tests if the file extension is correct
    def acceptExtension(file:File):Boolean ={    
      file.getName().endsWith(".mp3") ||  file.getName().endsWith(".wav") ||  file.getName().endsWith(".aiff") ||  file.getName().endsWith(".ogg")       
     }
    /*
   * add file audio selected with file selector
   * 
   */
  def addAudio():Unit={    
    val chooser =new FileChooser()
    chooser.fileFilter=(new filterExtension)	
    chooser.acceptAllFileFilter
    val result =chooser.showSaveDialog(null)
	if(result == FileChooser.Result.Approve ){
								println("etx ::"+acceptExtension(chooser.selectedFile))
								if(acceptExtension(chooser.selectedFile)){
    	  								gestAudio.putAudio(new Audio(chooser.selectedFile.getName(),chooser.selectedFile.getAbsolutePath()))
    	  								listView.listData = gestAudio.getAll
    	  								selectedAudio =""
    	  								println("add file :"+chooser.selectedFile.getName()+"add path :"+chooser.selectedFile.getAbsolutePath())
								}
								else {
								  println("Failed ! Accepted Extension : mp3 , wav , ogg, aiff")
								  button.text = "Failed ! Accepted Extension : mp3 , wav , ogg, aiff"
								}
										
	}
  }
  /*
   * delete audio file if selected 
   */
    def deleteAudio():Unit={    			
	    println("deleting :" +selectedAudio)
        if(gestAudio.delete(selectedAudio) != -1){
    	  	println("file deleted :"+selectedAudio)	
    	  	listView.listData = gestAudio.getAll
    	  	selectedAudio =""}
    	    //listView.listData=listView.listData.dropWhile( a => a.name==selectedAudio )
  }

    /*
     * update audio file information
     */
  def modifAudio(a:Audio) : Unit ={ 

    						val popup = new Dialog
		  					val form=new GridPanel(10,1){
  							val title= new Label("Audio:"+a.name )   
  							val n_autor= new TextField(a.autor)
							val n_album= new TextField(a.album )
							val n_year= new TextField(a.year.toString)
						    val n_genre= new TextField(a.genre) 
  							val submit= new Button("submit")
  							contents += title
  							contents+=new Label("Autor:" )
  							contents +=n_autor
  							contents+=new Label("Album:" )
  							contents +=n_album
  							contents+=new Label("Year:" )
  							contents +=n_year
  							contents+=new Label("Genre:" )
  							contents +=n_genre
  							contents +=submit
  							listenTo(submit)
  							reactions  += {					
  							case ButtonClicked(component) if component == submit => {var yearr=0 
  																					try {yearr =n_year.text.toInt} catch {case _ => yearr = 0}
  								 													var newAudio= new Audio(a.name,a.path,autor=n_autor.text,album=n_album.text,genre=n_genre.text,year=n_year.text.toInt)	 													
  								 													gestAudio.modifAudio(newAudio)
  								 													listView.listData = gestAudio.getAll
    	  	
  							   														} 
  							 }
		  					
  							}
  							popup.preferredSize = new Dimension(200,400)
  							popup.title = "Modifying... "
  							popup.contents =form
		  					popup.visible=true
		  					
		  				}

  
  // Our Main frame 
  def top = new MainFrame{
         preferredSize = new Dimension(500,600)
         title = "ScalaPlayer"
           //Menu
	    menuBar = new MenuBar {  
       contents += new MenuItem(Action("Auteurs"){ 
         val popupAutors=new Dialog()
         	popupAutors.contents = new TextArea("\n Huseyin / Steven / William \n hepia scala project \n 2014 Logiciel Libre \n")
	        popupAutors.title="Autors : "
	        popupAutors.preferredSize = new Dimension(200,200)
         	popupAutors.minimumSize   = new Dimension(200,100)
            popupAutors.location = new Point(400,400)
			popupAutors.visible=true
           })
       contents += new MenuItem(Action("Exit"){sys.exit(0)})
       

     }
  // this button is for showing warnings and some informations for users
  def newButton(tag : String) = new Button { text = tag } 
  val addBtn = newButton("Add")
  val deleteBtn = newButton("Delete")
  val modfBtn = newButton("Modify file")
  val playBtn = newButton("Play")
  val stopBtn = newButton("Stop")
  val controlVol = new Slider {	paintLabels = true
    							background = Color.WHITE
    							focusable =true
	  							max = 100
	  							min = 0
	  							title ="Volume"
	  							} 
  //buttons management audio
  val PanelBtns = new GridPanel(20,1){
    background = Color.LIGHT_GRAY
    contents += addBtn
    contents += deleteBtn
    contents += modfBtn    
  }
  //buttons control audio
  val PlayerBtns = new GridPanel(1,10){
    contents += playBtn
    contents += stopBtn
    contents += controlVol       
  }

             
      //  the audio file list
    val panel = new BoxPanel(Orientation.Vertical)
    panel.border = Swing.EmptyBorder(3)
    panel.contents += new ScrollPane(listView)

	//Our main frame contains a BorderPanel
	contents = new BorderPanel{
       layout(PlayerBtns) = South
       layout(button) = North
       layout(PanelBtns) = East
       layout(panel) = Center
     }
	  //add listener          
	listenTo(addBtn,deleteBtn,playBtn,stopBtn,modfBtn,controlVol,listView.selection)  // react to events
    reactions += {
      case ButtonClicked(component) if component == stopBtn => 	gestPlayer.stop ;println("stop") 
      
      case ButtonClicked(component) if component == playBtn =>  {   
    	  															if(gestPlayer != null ) gestPlayer.stop
    	  															gestPlayer= new AudioFile(listView.selection.items(0).path )	
      																gestPlayer.play ;println("play") }
      case ButtonClicked(component) if component == addBtn =>    addAudio
      case ButtonClicked(component) if component == deleteBtn => {
    	  													if(selectedAudio  != "")
    	  														deleteAudio 
    	  													else  {button.text = "First Select the file";println("select file") }
      }
      case ButtonClicked(component) if component == modfBtn  =>   {
    	  													if(selectedAudio  != "")
    	  														modifAudio(listView.selection.items(0)) 
    	  													else  {button.text = "First Select the file";println("select file") }
      }
      															
      // change the volum if slider moves
      case ValueChanged(controlVol:Slider) => 	gestPlayer.setVolume(controlVol.value.toFloat)

      // detection of the choise the audio
      case SelectionChanged(component) if component == listView   =>  {
    	  																try
    	  																{selectedAudio = listView.selection.items(0).name ;    																									 
    	  																button.text = "Let's Play Selected :"+selectedAudio	
      																	} 
    	  																catch {case _:Throwable => println("list updated")}
    	  																} 
    		}
	
  		}// end main frame
  

	}//end object



//filter for  filechooser for showing only accepted file extenions as mp3,wav,ogg,aiff
   class filterExtension extends javax.swing.filechooser.FileFilter{
     
    override def accept(file:File):Boolean ={
    
      file.getName().endsWith(".mp3") ||  file.getName().endsWith(".wav") ||  file.getName().endsWith(".aiff") ||  file.getName().endsWith(".ogg")       
     }
     override def getDescription():String={"mp3,wav,ogg,aiff"}
     
   }