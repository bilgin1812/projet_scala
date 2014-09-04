package audio

import java.io.File
import java.io.IOException
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine.Info
import javax.sound.sampled.LineUnavailableException
import javax.sound.sampled.SourceDataLine
import javax.sound.sampled.UnsupportedAudioFileException
import javax.sound.sampled.AudioSystem.getAudioInputStream
import javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED;
import javax.sound.sampled.FloatControl

/***
 * AudioFile2
 * Steven Destro
 * HEPIA
 * Can play wav, aiff, mp3, ogg files
 * Supports volume control
 */
class AudioFile (filename : String) {
  
	// INIT
	val f : File = new File(filename)
	val in : AudioInputStream = getAudioInputStream(f)
	val outFormat : AudioFormat = getOutFormat(in.getFormat())
	println("Loaded : " +filename + "\n" + outFormat.toString())
	val info : Info = new Info(classOf[SourceDataLine], outFormat)
	val line : SourceDataLine = AudioSystem.getLine(info).asInstanceOf[SourceDataLine]
	
	val VOLUMMAX : Float = 65536f
	var initVol : Boolean = false
	
	private var playing = false
	private var thPlay = new Thread()
	
	def play() {
	  thPlay = new Thread(new Runnable {
			def run {
				if (line != null) {
					line.open(outFormat)
					if (!initVol) {
					 setVolume(90.0f)
					 initVol = true
					}
					line.start()
					stream(getAudioInputStream(outFormat , in), line)
					line.drain()
					line.stop()
				}
			}
	  })
	  thPlay.start()
	  playing = true
	}
	
	private def stream(in : AudioInputStream, lin : SourceDataLine) : Unit = {
	  val buffer : Array[Byte] = new Array[Byte](4096)
	  var n : Int = 0
	  while (n != -1) {
	    n = in.read(buffer, 0, buffer.length)
	    lin.write(buffer, 0, n)
	  }
	}
	
	/**
	 * Return correct AudioFormat
	 */
	private def getOutFormat(inFormat : AudioFormat) : AudioFormat = {
	  val ch : Int = inFormat.getChannels()
	  val rate : Float = inFormat.getSampleRate()
	  new AudioFormat(PCM_SIGNED, rate, 16, ch, ch*2, rate, false)
	}
	
	/**
	 * Set volume of the music
	 */
	def setVolume (v : Float) { 
	  if (playing) {
	    val finalVol : Float = (VOLUMMAX  * v) / 100
	    val ctrl : FloatControl = line.getControl(FloatControl.Type.VOLUME).asInstanceOf[FloatControl]
	    ctrl.setValue(finalVol)
	    println(ctrl.toString())
	  }
	}
	
	
	
		/***
	 * Stop the music
	 */
	def stop { 
	  if (playing) {
		  thPlay.stop 
		  line.close()
		  playing = false
	  }   
	}
	
	
}
	



















	