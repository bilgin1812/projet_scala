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
  
  /*
   * add file audio selected with file selector
   * 
   */
  def addAudio():Unit={    
    val chooser =new FileChooser()
    val result =chooser.showSaveDialog(null)
	if(result == FileChooser.Result.Approve ){
										println("methode add appelé")
    	  								gestAudio.putAudio(new Audio(chooser.selectedFile.getName(),chooser.selectedFile.getAbsolutePath()))
    	  								listView.listData = gestAudio.getAll
    	  								println("add file :"+chooser.selectedFile.getName()+"add path :"+chooser.selectedFile.getAbsolutePath())										
  										}
  }
    def deleteAudio():Unit={    

	if(selectedAudio !=null ){
    	  								if(gestAudio.delete(selectedAudio) != -1)
    	  									println("file deleted :"+selectedAudio)	
    	  									listView.listData = gestAudio.getAll
    	  									//listView.listData=listView.listData.dropWhile( a => a.name==selectedAudio )
  										}
  }
  
    def getAudio(name :String) : Audio = {
      
      new Audio("tt","path")
    }
  def chooseFile(title: String=" "):Option[File] ={
		  val chooserF = new FileChooser()
		  val result =chooserF.showOpenDialog(null)
		  if(result == FileChooser.Result.Approve ){
		  				 println("Approve -- " + chooserF.selectedFile)
		  				Some(chooserF.selectedFile)
		  				}
		  else None		     
  }

  def modifAudio(a:Audio) : Unit ={ 

    						val popup = new Dialog
		  					val form=new GridPanel(6,1){
  							val title= new Button(a.name )  					
  							val n_autor= new TextField(a.autor)
							val n_album= new TextField(a.album )
							val n_year= new TextField(a.year)
						    val n_genre= new TextField(a.genre) 
  							val submit= new Button("submit")
  							contents += title
  							contents +=n_autor
  							contents +=n_album
  							contents +=n_year
  							contents +=n_genre
  							contents +=submit
  							listenTo(submit)
  							reactions  += {					
  							 case ButtonClicked(component) if component == submit => {
  								 													var newAudio= new Audio(a.name,a.path,autor=n_autor.text,album=n_album.text,genre=n_genre.text,year=n_year.text.toInt)	 													
  								 													gestAudio.modifAudio(newAudio)
  							   														} 
  							 }
		  					
  							}
  							popup.preferredSize = new Dimension(200,400)
  							popup.title = "Modifie and save the Audio file"
  							popup.contents =form
		  					popup.visible=true
		  					
		  				}

  
  
  def top = new MainFrame{
         preferredSize = new Dimension(500,600)
	  title = "ScalaPlayer"
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
  val PanelBtns = new GridPanel(20,1){
    background = Color.LIGHT_GRAY
    contents += addBtn
    contents += deleteBtn
    contents += modfBtn    
  }
  
  val PlayerBtns = new GridPanel(1,10){
    contents += playBtn
    contents += stopBtn
    contents += controlVol       
  }

  val button = new Button {
	  text =" Let's Play ! Choise your song."
      foreground = Color.red
      background = Color.PINK      
       }
  val panel = new BoxPanel(Orientation.Vertical)
    panel.border = Swing.EmptyBorder(3)
    panel.contents += new ScrollPane(listView)


	//  LayoutContainer = Area 
	contents = new BorderPanel{
       
	  //layout( new Button{text ="test"}) = West
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
    	  															gestPlayer= new AudioFile(selectedAudio)	
      																gestPlayer.play ;println("play") }
      case ButtonClicked(component) if component == addBtn =>    addAudio
      case ButtonClicked(component) if component == deleteBtn => deleteAudio
      case ButtonClicked(component) if component == modfBtn &&  (listView.selection.items(0).name  != null)=> modifAudio(listView.selection.items(0))
      															
      // change the volum if slider moves
      case ValueChanged(controlVol:Slider) => 	gestPlayer.setVolume(controlVol.value.toFloat)

      // detection of the choise the audio
      case SelectionChanged(component) if component == listView &&  !listView.selection.adjusting && listView.selection.items(0).name != null =>  {
    	  																								if(listView.selection.items(0).name != null)
    	  																									{selectedAudio = listView.selection.items(0).name ;    																									 
    	  																									button.text = "Let's Play Selected :"+selectedAudio	
      																										}   
    	  																								} 
    		}
	
  		}// end main frame
  

	}//end object
