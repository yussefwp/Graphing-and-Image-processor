import java.io.*;
import java.util.*;


public class WGraph
{    
    /////////////////////////////////////////////////////////////////
    // Inner data structure
    class WVertex
    {
        int x;
        int y;        
        
        int heapIndex;  // index in heap
        int distance;
        WVertex parent;
        char tag;   // tag: 'S'= start set, 'T' = target set  ' ' = normal
        
        ArrayList<WEdge> edges;
        
        public WVertex(int x, int y)
        {
            this.x = x;
            this.y = y;
            distance = Integer.MAX_VALUE;
            parent = null;
            tag = ' ';
            edges = new ArrayList<WEdge>();
            heapIndex = -1;
        }
    }
    
    class WEdge
    {
        int srcIdx;  // index from 0 to V-1
        int dstIdx;  // index from 0 to V-1
        int weight;
        
        public WEdge(int s, int d, int w)
        {
            srcIdx = s;
            dstIdx = d;
            weight = w;
        }        
    }  
    
    // heap class to store the vertices
    class WHeap
    {    
        WVertex[] data;      // heap store the nodes
        int size;
        
        // constructor for the heap
        public WHeap(int maxCapacity)
        {
            data = new WVertex[maxCapacity];
            size = 0;
        }
        
        // add the vertex into heap directly
        public void addDirectly(WVertex v)
        {
            data[size] = v;
            v.heapIndex = size;
            size++;
        }
        
        // return the size
        public int getSize()
        {
            return size;
        }
        
        private void swap(int i, int j)
        {
            WVertex w = data[i];
            data[i] = data[j];
            data[j] = w;
            
            data[i].heapIndex = i;
            data[j].heapIndex = j;
        }
        
        // remove 
        public WVertex removeMin()
        {
            WVertex min = data[0];
            size--;
            data[0] = data[size];
            data[0].heapIndex = 0;
            
            int pos = 0;
            int child = pos * 2 + 1;

            while (child < size)
            {
                // find the min child
                if (child + 1 < size && data[child+1].distance < data[child].distance)
                    child = child + 1;
                
                if (data[pos].distance <= data[child].distance)
                    break;

                swap(pos, child);
                
                pos = child;
                child = pos * 2 + 1;            
            }
            
            return min;
        }

        public void moveUp(WVertex v)
        {
            int pos = v.heapIndex;
            
            while (pos >= 1) 
            {
                int parent = (pos - 1) / 2;
                
                if (data[parent].distance <= data[pos].distance)
                    break;
                
                swap(pos, parent);
                
                pos = parent;            
            } 
        } 
    } 
    

    /////////////////////////////////////////////////////////////////
    // class member
    ArrayList<WVertex> vertices;
    
    public WGraph(String FName) throws IOException
    {
        vertices = new ArrayList<WVertex>();
        
        Scanner file = new Scanner(new File(FName));
        int numV = file.nextInt();
        int numE = file.nextInt();
        for (int i = 0; i < numE; i++)
        {
            int x1 = file.nextInt();
            int y1 = file.nextInt();
            int x2 = file.nextInt();
            int y2 = file.nextInt();
            int w = file.nextInt();
            
            int idx1 = getV(x1, y1, true);
            int idx2 = getV(x2, y2, true);
            
            // undirected graph
            vertices.get(idx1).edges.add(new WEdge(idx1, idx2, w));
            vertices.get(idx2).edges.add(new WEdge(idx2, idx1, w)); 
        }
        
        file.close();
    }
    
    // get vertex index, return -1 if not found
    //   if addIfNotFound = true, the method will add new vertex
    private int getV(int x, int y, boolean addIfNotFound)
    {
        for (int i = 0; i < vertices.size(); i++)
        {
            WVertex v = vertices.get(i);
            if (v.x == x && v.y == y)
                return i;
        }
        if (!addIfNotFound)
            return -1;
        vertices.add(new WVertex(x, y));
        return vertices.size() - 1;
    }
    
    // reset all vertices
    private void reset()
    {
        for (int i = 0; i < vertices.size(); i++)
        {
            WVertex v = vertices.get(i);
            v.distance = Integer.MAX_VALUE;
            v.parent = null;
            v.tag = ' ';
        }
    }
    
    // The pre/post-conditions describes the structure of the
    // input/ouput. The semantics of these structures depend on
    // defintion of the corresponding method.
    // pre: ux, uy, vx, vy are valid coordinates of vertices u and v
    // in the graph
    // post: return arraylist contains even number of integers,
    // for any even i,
    // i-th and i+1-th integers in the array represent
    // the x-coordinate and y-coordinate of the i/2-th vertex
    // in the returned path (path is an ordered sequence of vertices)
    ArrayList<Integer> V2V(int ux, int uy, int vx, int vy)
    {
        ArrayList<Integer> s1 = new ArrayList<Integer>();
        s1.add(ux);
        s1.add(uy);
        ArrayList<Integer> s2 = new ArrayList<Integer>();
        s2.add(vx);
        s2.add(vy);
        return S2S(s1, s2);
    }
 
    // pre: ux, uy are valid coordinates of vertex u from the graph
    // S represents a set of vertices.
    // The S arraylist contains even number of intergers
    // for any even i,
    // i-th and i+1-th integers in the array represent
    // the x-coordinate and y-coordinate of the i/2-th vertex
    // in the set S.
    // post: same structure as the last method's post.
    ArrayList<Integer> V2S(int ux, int uy, ArrayList<Integer> S)
    {
        ArrayList<Integer> s1 = new ArrayList<Integer>();
        s1.add(ux);
        s1.add(uy);
        
        return S2S(s1, S);
    }
    
    // pre: S1 and S2 represent sets of vertices (see above for
    // the representation of a set of vertices as arrayList)
    // post: same structure as the last method's post.
    ArrayList<Integer> S2S(ArrayList<Integer> S1, ArrayList<Integer> S2)
    {
        ArrayList<Integer> result = new ArrayList<Integer>();
        reset();
        WHeap heap = new WHeap(vertices.size());
        
        // initialize the S1 vertices
        for (int i = 0; i < S1.size(); i += 2)
        {
            int x = S1.get(i);
            int y = S1.get(i+1);
            
            int idx = getV(x, y, false);
            if (idx < 0)
                return result;
            
            WVertex v = vertices.get(idx);
            v.distance = 0;
            v.tag = 'S';
            heap.addDirectly(v);
        }

        // initialize the S2 vertices
        for (int i = 0; i < S2.size(); i += 2)
        {
            int x = S2.get(i);
            int y = S2.get(i+1);
            
            int idx = getV(x, y, false);
            if (idx < 0)
                return result;
            
            WVertex v = vertices.get(idx);
            v.tag = 'T';
        }
        
        // add other vertices to heap
        for (int i = 0; i < vertices.size(); i++)
        {
            WVertex v = vertices.get(i);
            if (v.tag != 'S')
                heap.addDirectly(v);
        }
        

        while (heap.getSize() > 0) 
        {
            WVertex minv = heap.removeMin();
            if (minv.tag == 'T')
            {
                // find the path
                ArrayList<WVertex> list = new ArrayList<WVertex>();
                while (minv.tag != 'S')
                {
                    list.add(minv);
                    minv = minv.parent;
                }
                list.add(minv);
                
                // copy to result
                for (int i = list.size() - 1; i >= 0; i--)
                {
                    result.add(list.get(i).x);
                    result.add(list.get(i).y);
                }
                
                break;
            }
            minv.heapIndex = -1;  // it is removed from heap

            for (WEdge e : minv.edges)
            {
                int nd = minv.distance + e.weight;
                
                WVertex v = vertices.get(e.dstIdx);
                if (v.distance > nd && v.heapIndex >= 0) 
                {
                    v.distance = nd;
                    v.parent = minv;
                    heap.moveUp(v);
                }
            }            
        }        
        
        return result;
    }
    
    public static void main(String[] args) throws IOException
    {
        WGraph g = new WGraph("graph.txt");
        ArrayList<Integer> result = g.V2V(1, 2, 3, 4);
        for (int i = 0; i < result.size(); i+=2)
        {
            System.out.println(result.get(i) + "," + result.get(i+1));
        }
    }
    
}
