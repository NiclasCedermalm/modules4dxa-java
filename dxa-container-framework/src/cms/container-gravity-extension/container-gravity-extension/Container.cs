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
                    if (currentContainer != null) 
                    {
                        ComponentPresentationInfo cpInfo = currentContainer.AddToComponentPresentationList(cp);
                    }
                }
                pageIndex++; 
            }
       
            return containers;
        }

        /// <summary>
        /// Check if provided component presentation is a container component or not.
        /// </summary>
        /// <param name="componentPresentation"></param>
        /// <returns>bool</returns>
        static public bool IsContainer(ComponentPresentation componentPresentation)
        {
            return GetContainerName(componentPresentation.ComponentTemplate) != null;
        }

        /// <summary>
        /// Extract container index from component presentation.
        /// </summary>
        /// <param name="componentPresentation"></param>
        /// <returns></returns>
        static public int ExtractContainerIndex(ComponentPresentationInfo componentPresentation)
        {
            return ExtractContainerIndex(componentPresentation.ComponentPresentation.ComponentTemplate.Id);
        }

        /// <summary>
        /// Extract container index from template TCM-URI (where the index is piggybacked as ID version)
        /// </summary>
        /// <param name="templateUri"></param>
        /// <returns></returns>
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

        /// <summary>
        /// Remove container index from Template TCM-URI
        /// </summary>
        /// <param name="templateUri"></param>
        /// <returns></returns>
        static public TcmUri RemoveContainerIndex(TcmUri templateUri)
        {
            return templateUri.GetVersionlessUri();
        }

        /// <summary>
        /// Get container name
        /// </summary>
        /// <param name="containerTemplate"></param>
        /// <returns></returns>
        static public string GetContainerName(ComponentTemplate containerTemplate)
        {
            if ( containerTemplate != null && containerTemplate.Metadata != null && containerTemplate.MetadataSchema != null )
            {
                var metadata = new ItemFields(containerTemplate.Metadata, containerTemplate.MetadataSchema);
                if (metadata.Contains("routeValues"))
                {
                    TextField routeValues = (TextField) metadata["routeValues"];
                    if (routeValues.Values.Count() > 0 && routeValues.Value.Contains("containerRegion"))
                    {
                        return routeValues.Value.Replace("containerRegion:", "");
                    }
                }
            }        
            return null;
        }

        /// <summary>
        /// Protecte constructor
        /// </summary>
        /// <param name="page"></param>
        /// <param name="containerComponent"></param>
        /// <param name="containerTemplate"></param>
        protected Container(Page page, Component containerComponent, ComponentTemplate containerTemplate)
        {
            this.page = page;
            this.containerComponent = containerComponent;
            this.ComponentPresentations = new List<ComponentPresentationInfo>();
            this.containerName = GetContainerName(containerTemplate);
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

        public IList<ComponentPresentationInfo> ComponentPresentations { get; private set; }

        private ComponentPresentationInfo AddToComponentPresentationList(ComponentPresentation componentPresentation)
        {
            ComponentPresentationInfo cpInfo = new ComponentPresentationInfo(this.page, componentPresentation, this);
            this.ComponentPresentations.Add(cpInfo);
            return cpInfo;
        }

        /// <summary>
        /// Add component presentation the container
        /// </summary>
        /// <param name="componentPresentation"></param>
        public void Add(ComponentPresentation componentPresentation)
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
