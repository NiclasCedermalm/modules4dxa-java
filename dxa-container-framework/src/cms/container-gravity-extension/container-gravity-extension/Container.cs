using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using Tridion.ContentManager;
using Tridion.ContentManager.CommunicationManagement;
using Tridion.ContentManager.ContentManagement;
using Tridion.ContentManager.ContentManagement.Fields;
using Tridion.Logging;

namespace SDL.DXA.Extensions.Container
{
    /// <summary>
    /// Container representation
    /// </summary>
    public class Container
    {
        protected Page page;
        protected Component containerComponent;
        private string containerName;
        private int index;
        private int pageIndex;
    
        /// <summary>
        /// Get all container components on a page
        /// </summary>
        /// <param name="page"></param>
        /// <returns></returns>
        static public IList<Container> GetContainers(Page page)
        {
            int containerCount = 0;
            int pageIndex = 0;
            IList<Container> containers = new List<Container>();
            Container currentContainer= null;

            // TODO: Have a better page index calculation algorithm that can handle reshuffle of component presentations
            //

            foreach (ComponentPresentation cp in page.ComponentPresentations )
            {             
                if ( IsContainer(cp) )
                {                  
                    currentContainer = new Container(page, cp.Component, cp.ComponentTemplate);
                    currentContainer.Index = ++containerCount;
                    currentContainer.pageIndex = pageIndex;
                    containers.Add(currentContainer);
                }
                else
                {
                    if (currentContainer != null && currentContainer.Owns(cp)) 
                    {
                        ComponentPresentationInfo cpInfo = currentContainer.AddToComponentPresentationList(cp);
                    }
                }
                pageIndex++; // This works only if we move the last item around....
            }
       
            return containers;
        }

        static public bool IsContainer(ComponentPresentation componentPresentation)
        {
            // TODO: Have a better indication algorithm for containers
            return componentPresentation.Component.Schema.Title.Contains("Container");
        }

        static public int ExtractContainerIndex(ComponentPresentationInfo componentPresentation)
        {
            return ExtractContainerIndex(componentPresentation.ComponentPresentation.ComponentTemplate.Id);
        }

        static public int ExtractContainerIndex(TcmUri templateUri)
        {
            String itemId = templateUri.ItemId.ToString();
            int containerIndex = templateUri.Version;
            
            if ( !templateUri.IsVersionless )
            {
                return containerIndex;
            }
            return -1;
        }

        static public TcmUri RemoveContainerIndex(TcmUri templateUri)
        {
            return templateUri.GetVersionlessUri();
        }

        protected Container(Page page, Component containerComponent, ComponentTemplate containerTemplate)
        {
            this.page = page;
            this.containerComponent = containerComponent;
            this.ComponentPresentations = new List<ComponentPresentationInfo>();
            var metadata = new ItemFields(containerTemplate.Metadata, containerTemplate.MetadataSchema);
            TextField routeValues = (TextField) metadata["routeValues"];
            this.containerName = routeValues.Value.Replace("containerRegion:", "");
        }

        public String Name 
        {
            get { return this.containerComponent.Title; }
        }
 
        public int Index
        {
            get { return this.index; }
            internal set { this.index = value; }
        }

        public int PageIndex
        {
            get { return this.pageIndex; }
            set { this.pageIndex = value; }
        }

        public bool Owns(ComponentPresentation componentPresentation)
        {
            // TODO: Is the component template invalid in this case when using the container index??
            /*
            var metadata = new ItemFields(componentPresentation.ComponentTemplate.Metadata, componentPresentation.ComponentTemplate.MetadataSchema);
            if ( metadata.Contains("regionName") )
            {
                TextField regionName = (TextField)metadata["regionName"];
                return regionName.Value.Equals(this.containerName);
            }
            return false;
            */
            return true;
        }

        public IList<ComponentPresentationInfo> ComponentPresentations { get; private set; }

        private ComponentPresentationInfo AddToComponentPresentationList(ComponentPresentation componentPresentation)
        {
            ComponentPresentationInfo cpInfo = new ComponentPresentationInfo(this.page, componentPresentation, this);
            this.ComponentPresentations.Add(cpInfo);
            return cpInfo;
        }

        public virtual void Add(ComponentPresentation componentPresentation)
        {
            Logger.Write("Adding CP to page index: " + pageIndex, "RegionGravityHandler", LogCategory.Custom, TraceEventType.Information);
            this.AddToComponentPresentationList(componentPresentation);
            page.ComponentPresentations.Insert(pageIndex + this.ComponentPresentations.Count(), componentPresentation);
            
        }

    }


    /// <summary>
    /// Component Presentation Information
    /// </summary>
    public class ComponentPresentationInfo
    {

        public ComponentPresentationInfo(Page page, ComponentPresentation componentPresentation, Container owner)
        {
            ComponentPresentation = componentPresentation;
            ContainerIndex = Container.ExtractContainerIndex(componentPresentation.ComponentTemplate.Id);
            Owner = owner;
        }

        public ComponentPresentation ComponentPresentation { get; private set; }
   
        public int ContainerIndex { get; private set; }
        public Container Owner { get; private set; }
    }
}
