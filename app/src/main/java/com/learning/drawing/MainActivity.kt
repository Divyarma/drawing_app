 package com.learning.drawing

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_brush_color.*
import kotlinx.android.synthetic.main.dialog_brush_size.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    companion object{
        private const val READ_and_WRITE=1
        private const val GALLERY=2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawing_view.SetSizeForBrush(20.0f)
        ib_brushsize.setOnClickListener {
            brushsizedialog()
        }
        ib_brushcolor.setOnClickListener {
            brushColordialog()
        }
        ib_imagebg.setOnClickListener {
            if(isReadAllowed()){

                val  pickPhotointent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(pickPhotointent,GALLERY)
            }
            else{
                requestStorage()
            }
        }
        ib_undo.setOnClickListener {
            drawing_view.onClickUndo()
        }
        ib_save.setOnClickListener {
            if(isReadAllowed()){
                BitmapAsync(getBitmap(fram)).execute()
            }
            else{
                requestStorage()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode==Activity.RESULT_OK){
            if(requestCode== GALLERY){
                try{
                    if(data!!.data!=null){
                        imageView2.visibility= View.VISIBLE
                        imageView2.setImageURI(data.data)
                    }else{
                        Toast.makeText(this,"Error in parsing the image or its corrupted",Toast.LENGTH_SHORT).show()
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
        }
    }

    private fun requestStorage(){
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE), READ_and_WRITE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode== READ_and_WRITE){
            if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Granted",Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(this,"Permission denied",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isReadAllowed():Boolean{
        val result=ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)
        return (result==PackageManager.PERMISSION_GRANTED)
    }

    private fun brushsizedialog(){

        Log.i("position ","in the function")
        val brushdialog=Dialog(this)
        brushdialog.setContentView(R.layout.dialog_brush_size)
        brushdialog.setTitle("Brush Size : ")
        (brushdialog.ib_small_brush).setOnClickListener{
            drawing_view.SetSizeForBrush(10.0f)
            brushdialog.dismiss()
        }
        (brushdialog.ib_medium_brush).setOnClickListener{
            drawing_view.SetSizeForBrush(20.0f)
            brushdialog.dismiss()
        }
        (brushdialog.ib_large_brush).setOnClickListener{
            drawing_view.SetSizeForBrush(30.0f)
            brushdialog.dismiss()
        }

        brushdialog.show()
    }

    private fun brushColordialog(){
        val brushdialog=Dialog(this)
        brushdialog.setContentView(R.layout.dialog_brush_color)
        brushdialog.setTitle("Brush Color : ")
        (brushdialog.ib_black_brush).setOnClickListener{
            drawing_view.SetColourForBrush(Color.BLACK)
            brushdialog.dismiss()
        }
        (brushdialog.ib_green_brush).setOnClickListener{
            drawing_view.SetColourForBrush(Color.GREEN)
            brushdialog.dismiss()
        }
        (brushdialog.ib_red_brush).setOnClickListener{
            drawing_view.SetColourForBrush(Color.RED)
            brushdialog.dismiss()
        }
        (brushdialog.ib_blue_brush).setOnClickListener{
            drawing_view.SetColourForBrush(Color.BLUE)
            brushdialog.dismiss()
        }
        brushdialog.show()
    }

    private fun getBitmap(view:View):Bitmap{
        val bitmap_Return= createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
        val canvas=Canvas(bitmap_Return)
        val bgDrawable=view.background
        if(bgDrawable!=null){
            bgDrawable.draw(canvas)
        }
        else{
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return bitmap_Return
    }

    @Suppress("DEPRECATION")
    private inner class BitmapAsync(val mBitmap:Bitmap): AsyncTask<Any,Void,String>(){

        val dial=Dialog(this@MainActivity)

        override fun onPreExecute() {
            super.onPreExecute()

            dial.setContentView(R.layout.custom_progress)
            dial.show()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            dial.dismiss()
            if(!result!!.isEmpty()){
                Toast.makeText(this@MainActivity,"Saved",Toast.LENGTH_SHORT).show()
            }
            else{
            Toast.makeText(this@MainActivity,"somthing went wrong while saving",Toast.LENGTH_SHORT).show()
            }
            MediaScannerConnection.scanFile(this@MainActivity, arrayOf(result),null){
                path,uri->val shareIntent=Intent()
                shareIntent.action=Intent.ACTION_SEND
                shareIntent.putExtra(Intent.EXTRA_STREAM,uri) 
                shareIntent.type="image/png"
                startActivity(
                        Intent.createChooser(shareIntent,"Share")
                )
            }
        }
        override fun doInBackground(vararg params: Any?): String? {

            var result=""
            if(mBitmap!=null){
                try{
                    val bytes=ByteArrayOutputStream()
                    mBitmap.compress(Bitmap.CompressFormat.PNG,90,bytes)
                    val f=File(externalCacheDir!!.absoluteFile.toString() + File.separator+"drawingapp__"+System.currentTimeMillis()/1000+".png")
                    val fo=FileOutputStream(f)
                    fo.write(bytes.toByteArray())
                    fo.close()
                    result=f.absolutePath
                }catch (e:Exception){
                    result=""
                    e.printStackTrace()
                }
            }
            return result

        }

    }
}