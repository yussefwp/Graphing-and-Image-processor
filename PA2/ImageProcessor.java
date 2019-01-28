import java.io.*;
import java.util.*;

public class ImageProcessor
{
    /////////////////////////////////////////////////////////////////
    // Inner data structure
    class Pixel
    {
        int r;
        int g;
        int b;
        
        public Pixel(int r, int g, int b)
        {
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }
    
    class ImgPath
    {
        int row;   // row index in the image, 0-H-1
        int col;   // column index, 0-W-1
        
        int sumCost;  // total cost
        
        ImgPath parent;
        
        public ImgPath(int row, int col, int cost, ImgPath p)
        {
            this.row = row;
            this.col = col;
            this.parent = p;
            this.sumCost = cost;
            if (p != null)
                this.sumCost += p.sumCost;
        }        
    }

    // heap class to store the path
    class IHeap
    {    
        ArrayList<ImgPath> data;      // heap store the nodes
        int size;
        
        // constructor for the heap
        public IHeap()
        {
            data = new ArrayList<ImgPath>();
            size = 0;
        }
        
        // add the path into heap
        public void add(ImgPath p)
        {
            if (size < data.size())
                data.set(size, p);
            else
                data.add(p);
            size++;
            
            // move up
            moveUp(size - 1);
        }
        
        // return the size
        public int getSize()
        {
            return size;
        }
        
        private void swap(int i, int j)
        {
            ImgPath w = data.get(i);
            data.set(i, data.get(j));
            data.set(j, w);
        }
        
        // remove 
        public ImgPath removeMin()
        {
            ImgPath min = data.get(0);
            size--;
            data.set(0, data.get(size));
            
            int pos = 0;
            int child = pos * 2 + 1;

            while (child < size)
            {
                // find the min child
                if (child + 1 < size && data.get(child+1).sumCost < data.get(child).sumCost)
                    child = child + 1;
                
                if (data.get(pos).sumCost <= data.get(child).sumCost)
                    break;

                swap(pos, child);
                
                pos = child;
                child = pos * 2 + 1;            
            }
            
            return min;
        }

        public void moveUp(int pos)
        {            
            while (pos >= 1) 
            {
                int parent = (pos - 1) / 2;
                
                if (data.get(parent).sumCost <= data.get(pos).sumCost)
                    break;
                
                swap(pos, parent);
                
                pos = parent;            
            } 
        } 
    } 
    
    ////////////////////////////////////////////////////////
    // class members
    
    ArrayList<ArrayList<Pixel>> pixels;
    
    
    public ImageProcessor(String FName) throws IOException
    {
        Scanner file = new Scanner(new File(FName));
        int H = file.nextInt();
        int W = file.nextInt();
        pixels = new ArrayList<ArrayList<Pixel>>();
        for (int i = 0; i < H; i++)
        {
            pixels.add(new ArrayList<Pixel>());
            for (int j = 0; j < W; j++)
            {
                int r = file.nextInt();
                int g = file.nextInt();
                int b = file.nextInt();
                pixels.get(i).add(new Pixel(r, g, b));
            }
        }
        
        file.close();        
    }
    
    // calculate and return the distance
    private int pdist(Pixel p, Pixel q)
    {
        int dr = p.r - q.r;
        int dg = p.g - q.g;
        int db = p.b - q.b;
        return dr * dr + dg * dg + db * db;
    }
    
    // pre:
    // post: returns the 2-D matrix I as per its definition
    ArrayList<ArrayList<Integer>> getImportance()
    {
        ArrayList<ArrayList<Integer>> mt = new ArrayList<ArrayList<Integer>>();
        int W = pixels.get(0).size();
        int H = pixels.size();
               
        for (int i = 0; i < H; i++)
        {
            mt.add(new ArrayList<Integer>());
            for (int j = 0; j < W; j++)
            {
                // calculate ximportant
                int xim = 0;
                if (j == 0)
                    xim = pdist(pixels.get(i).get(W-1), pixels.get(i).get(j+1));
                else if (j == W-1)
                    xim = pdist(pixels.get(i).get(j-1), pixels.get(i).get(0));
                else
                    xim = pdist(pixels.get(i).get(j-1), pixels.get(i).get(j+1));
                
                // calculate yimportant
                int yim = 0;
                if (i == 0)
                    yim = pdist(pixels.get(H-1).get(j), pixels.get(i+1).get(j));
                else if (i == H-1)
                    yim = pdist(pixels.get(i-1).get(j), pixels.get(0).get(j));
                else
                    yim = pdist(pixels.get(i-1).get(j), pixels.get(i+1).get(j));
                
                
                mt.get(i).add(xim+yim);
            }
        }        
        
        return mt;
    }
    
    // pre: W-k > 1
    // post: Compute the new image matrix after reducing the width by k
    // Follow the method for reduction described above
    // Write the result in a file named FName
    // in the same format as the input image matrix
    void writeReduced(int k, String FName) throws IOException
    {
        ArrayList<ArrayList<Pixel>> orgCopy = new ArrayList<ArrayList<Pixel>>();
        // make a copy
        for (int i = 0; i < pixels.size(); i++)
        {
            orgCopy.add(new ArrayList<Pixel>());
            for (int j = 0; j < pixels.get(i).size(); j++)
                orgCopy.get(i).add(pixels.get(i).get(j));
        }
        
        
        for (int i = 0; i < k; i++)
        {
            cut();
        }
        
        // write pixels to file
        PrintWriter pw = new PrintWriter(FName);
        pw.println(pixels.size());
        pw.println(pixels.get(0).size());
        for (int i = 0; i < pixels.size(); i++)
        {
            for (int j = 0; j < pixels.get(i).size(); j++)
            {
                if (j > 0)
                    pw.print(" ");
                Pixel p = pixels.get(i).get(j);
                pw.print(p.r + " " + p.g + " " + p.b);
            }
            pw.println();
        }
        pw.close();
        
        pixels = orgCopy;      
        
    }
    
    // make a cut
    void cut()
    {
        int H = pixels.size();
        int W = pixels.get(0).size();
        ArrayList<ArrayList<Integer>> im = getImportance();        
        
        IHeap heap = new IHeap();
        
        // add first row
        for (int i = 0; i < W; i++)
            heap.add(new ImgPath(0, i, im.get(0).get(i), null));
        
        ImgPath path = null;
        while (heap.getSize() > 0)
        {
            ImgPath m = heap.removeMin();
            if (m.row == H-1)
            {
                path = m;
                break;
            }
            int r = m.row + 1;
            for (int c = m.col - 1; c <= m.col + 1; c++)
            {
                if (c >= 0 && c < W)
                {
                    heap.add(new ImgPath(r, c, im.get(r).get(c), m));
                }
            }            
        }
        
        while (path != null)
        {
            pixels.get(path.row).remove(path.col);          
            path = path.parent;
        }      
    }

    public static void main(String[] args) throws IOException
    {
        ImageProcessor g = new ImageProcessor("image.txt");
        g.writeReduced(1, "imageCut.txt");
    }
}
