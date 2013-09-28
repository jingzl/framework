package cm.framework.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * 
 * @author gyx
 * 
 * 功能：文件管理工具
 *
 */
public class FileUtil
{

  /**
   * @param f
   * @return
   * @throws Exception
   * 功能：获取文件夹大小
   */
  public static long getFileSize(File f)throws Exception
  {
      long size = 0;
      File flist[] = f.listFiles();
      for (int i = 0; i < flist.length; i++)
      {
          if (flist[i].isDirectory())
          {
              size = size + getFileSize(flist[i]);
          } else
          {
              size = size + flist[i].length();
          }
      }
      return size;
  }
  
  /**  
   * 删除目录（文件夹）以及目录下的文件  
   * @param   sPath 被删除目录的文件路径  
   * @return  目录删除成功返回true，否则返回false  
   */  
  public static boolean deleteDirectory(String sPath) {   
      //如果sPath不以文件分隔符结尾，自动添加文件分隔符   
      if (!sPath.endsWith(File.separator)) {   
          sPath = sPath + File.separator;   
      }   
      File dirFile = new File(sPath);   
      //如果dir对应的文件不存在，或者不是一个目录，则退出   
      if (!dirFile.exists() || !dirFile.isDirectory()) {   
          return false;   
      }   
      boolean flag = true;   
      //删除文件夹下的所有文件(包括子目录)   
      File[] files = dirFile.listFiles();   
      for (int i = 0; i < files.length; i++) {   
          //删除子文件   
          if (files[i].isFile()) {   
              flag = deleteFile(files[i].getAbsolutePath());   
              if (!flag) break;   
          } //删除子目录   
          else {   
              flag = deleteDirectory(files[i].getAbsolutePath());   
              if (!flag) break;   
          }   
      }   
      if (!flag) return false;   
      //删除当前目录   
      if (dirFile.delete()) {   
          return true;   
      } else {   
          return false;   
      }   
  }  
  
  /**  
   * 删除单个文件  
   * @param   sPath    被删除文件的文件名  
   * @return 单个文件删除成功返回true，否则返回false  
   */  
  public static boolean deleteFile(String sPath) {   
      boolean flag = false;   
      File file = new File(sPath);   
      // 路径为文件且不为空则进行删除   
      if (file.isFile() && file.exists()) {   
          file.delete();   
          flag = true;   
      }   
      return flag;   
  }  
  
  /**
   * @param fileS
   * @return
   * 功能：转换文件大小
   */
  public static String formetFileSize(long fileS) {
    DecimalFormat df = new DecimalFormat("#.00");
    String fileSizeString = "";
    if (fileS < 1024) {
        fileSizeString = df.format((double) fileS) + "B";
    } else if (fileS < 1048576) {
        fileSizeString = df.format((double) fileS / 1024) + "K";
    } else if (fileS < 1073741824) {
        fileSizeString = df.format((double) fileS / 1048576) + "M";
    } else {
        fileSizeString = df.format((double) fileS / 1073741824) + "G";
    }
    return fileSizeString;
  }
  
  /**
   * 
   * @param fileName
   * @return
   * 功能：读取图片文件
   */
  public static Bitmap readBitmap(String fileName) {

    File file = new File(fileName);

    if (file.exists()) {
      FileInputStream fileInputStream;
      try {
        fileInputStream = new FileInputStream(file);
        Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream);
        return bitmap;
      } catch ( Throwable t ) {
        Log.e ( "FileUtil", "Exception readBitmap", t );
        return null;
      }

    } else {
      return null;
    }
  }
  
  /**
   * 
   * @param bitmap
   * @param fileName
   * 功能：创建图片文件
   */
  public static void saveBitmap(Bitmap bitmap, String fileName) {
    File file = new File(fileName);

    if (file.exists()) {
      file.delete();
    }

    try {
      file.createNewFile();
    } catch (IOException e1) {
      e1.printStackTrace();
    }

    FileOutputStream out;

    try {
      out = new FileOutputStream(file);
      if (bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)) {
        out.flush();
        out.close();
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
