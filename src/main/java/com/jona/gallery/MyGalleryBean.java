package com.jona.gallery;

import org.primefaces.event.FileUploadEvent;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.Serializable;

@ManagedBean
@ViewScoped
public class MyGalleryBean implements Serializable
{
    private List<MyPhoto> photos = new ArrayList<>();
    private static final int BUFFER_SIZE = 6124;

    // Como es un ViewScoped Bean, el constructor
    // se llama una vez por view
    public MyGalleryBean()
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        Path path = Paths.get(((ServletContext) externalContext.getContext())
                .getRealPath(File.separator + "resources" + File.separator + "photos"));
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(path))
        {
            for (Path file : ds)
            {
                MyPhoto photo = new MyPhoto(file.getFileName().toString(), false);
                photos.add(photo);
            }
        }
        catch (IOException ex)
        {
            Logger.getLogger(MyGalleryBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public List<MyPhoto> getPhotos() {
        return photos;
    }

    public void setPhotos(List<MyPhoto> photos) {
        this.photos = photos;
    }

    public void handleFileUpload(FileUploadEvent event)
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();

        if (event.getFile() != null)
        {
            Path path = Paths.get(((ServletContext) externalContext.getContext())
                    .getRealPath(File.separator + "resources" + File.separator + "photos" + File.separator));
            FileOutputStream fileOutputStream;
            InputStream inputStream;
            try
            {
                String fn = event.getFile().getFileName();
                fileOutputStream = new FileOutputStream(path.toString() + File.separator + fn);

                byte[] buffer = new byte[BUFFER_SIZE];

                int bulk;
                inputStream = event.getFile().getInputstream();

                bulk = inputStream.read(buffer);
                while (bulk >= 0)
                {
                    fileOutputStream.write(buffer, 0, bulk);
                    fileOutputStream.flush();

                    bulk = inputStream.read(buffer);
                }

                fileOutputStream.close();
                inputStream.close();

                MyPhoto newPhoto = new MyPhoto(fn, false);
                photos.add(newPhoto);

                facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Files were successfully uploaded!", null));
            }
            catch (FileNotFoundException ex)
            {
                facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "File " + event.getFile().getFileName() + " cannot be found!", null));
                Logger.getLogger(MyGalleryBean.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (IOException ex)
            {
                facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "File " + event.getFile().getFileName() + " cannot be uploaded!", null));
                Logger.getLogger(MyGalleryBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else
        {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "File does not exist!", null));
            Logger.getLogger(MyGalleryBean.class.getName()).log(Level.SEVERE, "File does not exist", "File does not exist");
        }
    }
}
