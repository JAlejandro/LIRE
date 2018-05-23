package net.semanticmetadata.lire.imageanalysis.features.global;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;
import net.semanticmetadata.lire.imageanalysis.features.global.dcm.DcmDump;
import org.dcm4che3.io.DicomInputStream;

/**
 * @author Jose Alejandro Camiño Iglesias
 */
public class DCMFeature implements GlobalFeature, LireFeature {
  private static List<String> names = new LinkedList<>();
  Map<String,String> feature = new HashMap<>();

  public static synchronized void addFileName(String string){
    names.add(string);
  }

  public static synchronized String removeFirst(){
    return names.remove(0);
  }


  @Override
  public void extract(BufferedImage image) {
    String fileName = removeFirst();
    if(fileName != null) {
      try {
        File f = new File(fileName);
        DicomInputStream inputStream = new DicomInputStream(f);
        DcmDump d = new DcmDump();
        inputStream.setDicomInputHandler(d);
        org.dcm4che3.data.Attributes attributes = inputStream.readDataset(-1, -1);
        System.out.println(d.map.toString());
        d.map.clear();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public String getFeatureName() {
    return "DCM properties";
  }

  @Override
  public String getFieldName() {
    return "Patient";
  }

  @Override
  public byte[] getByteArrayRepresentation() {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutput out = null;
    byte[] bytes = null;
    try {
      out = new ObjectOutputStream(bos);
      out.writeObject(feature);
      out.flush();
      bytes = bos.toByteArray();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        bos.close();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
    return bytes;
  }

  @Override
  public void setByteArrayRepresentation(byte[] featureData) {
    ByteArrayInputStream bis = new ByteArrayInputStream(featureData);
    try (ObjectInput in = new ObjectInputStream(bis)) {
      feature = (Map<String, String>) in.readObject();
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
    }
    // ignore close exception
  }

  @Override
  public void setByteArrayRepresentation(byte[] featureData, int offset,
      int length) {
    setByteArrayRepresentation(featureData);
  }

  @Override
  public double getDistance(LireFeature feature) {
    double distance = 0;
    if(feature instanceof DCMFeature){
      DCMFeature dcmFeature = (DCMFeature)feature;
      for(String k : this.feature.keySet()){
        String s1 = this.feature.get(k);
        String s2 = ((DCMFeature) feature).feature.get(k);
        if(s1 != null && s2 != null) {
          distance += Double.parseDouble(s1) - Double.parseDouble(s2);
        }
      }
    } else {
      distance = Long.MAX_VALUE;
    }
    return distance;
  }

  @Override
  public double[] getFeatureVector() {
    double[] toReturn = new double[feature.size()];
    int i = 0;
    for(String s : feature.values()){
      toReturn[i++] = Double.parseDouble(s);
    }
    return toReturn;
  }
}
